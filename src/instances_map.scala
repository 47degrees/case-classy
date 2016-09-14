/* -
 * Case Classy [case-classy]
 */

package classy

import shapeless._
//import shapeless.Typeable
//import shapeless.TypeCase

import cats.data._

import DecodeError._

object map extends MapImplicits0 {

  type MapDecoder[A] = Decoder[Map[String, Any], A]
  object MapDecoder {
    def apply[A](implicit ev: MapDecoder[A]): MapDecoder[A] = ev
  }

  def deriveMapDecoder[A](implicit decoderV: DecoderV[Map[String, Any], A]): MapDecoder[A] =
    Decoder(decoderV)

}

sealed trait MapImplicits0 extends MapImplicits1 {
  private[this] val `Map[String, Any]` = TypeCase[Map[String, Any]]

  implicit def yyzReadNestedMapSupport[A: DecoderV[Map[String, Any], ?]]: ReadValue[Map[String, Any], A] =
    ReadValue((map, key) ⇒ map.get(key) match {
      case Some(`Map[String, Any]`(submap)) ⇒
        DecoderV[Map[String, Any], A].apply(submap)
          .leftMap(errors ⇒ NonEmptyList(AtPath(key, errors), Nil))
      case None ⇒ Validated.invalidNel(MissingKey(key))
    })
}

sealed trait MapImplicits1 {
  implicit def yyzReadMapSupport[A: Typeable]: ReadValue[Map[String, Any], A] =
    ReadValue((map, key) ⇒ {
      val typeCase = TypeCase[A]
      map.get(key) match {
        case Some(typeCase(value)) ⇒ Validated.valid(value)
        case Some(other)           ⇒ Validated.invalidNel(WrongType(key, Typeable[A].describe))
        case _                     ⇒ Validated.invalidNel(MissingKey(key))
      }
    })
}
