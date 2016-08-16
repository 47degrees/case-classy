/* -
 * Case Classy [case-classy]
 */

package classy

import shapeless.Typeable
import shapeless.TypeCase

import cats.data._

object map {
  import DecodeError._

  implicit def yyzReadMapSupport[A: Typeable]: ReadValue[Map[String, Any], A] =
    ReadValue((map, key) ⇒ {
      val typeCase = TypeCase[A]
      map.get(key) match {
        case Some(typeCase(value)) ⇒ Validated.valid(value)
        case Some(other)           ⇒ Validated.invalidNel(WrongType(key))
        case _                     ⇒ Validated.invalidNel(MissingKey(key))
      }
    })
}