/* -
 * Case Classy [case-classy-typesafe]
 */

package classy

import cats.data._

import com.typesafe.config.Config
import com.typesafe.config.ConfigException

import scala.reflect.{ classTag, ClassTag }

object typesafe {
  import DecodeError._

  type TypesafeDecoder[A] = Decoder[Config, A]
  object TypesafeDecoder {
    def apply[A](implicit ev: TypesafeDecoder[A]): TypesafeDecoder[A] = ev
  }

  def deriveTypesafeDecoder[A](implicit decoderV: DecoderV[Config, A]): TypesafeDecoder[A] =
    Decoder(decoderV)

  private[this] def guard[A: ClassTag](f: ⇒ A, key: String): ResV[A] =
    Validated.catchNonFatal(f).leftMap {
      case e: ConfigException.Missing   ⇒ NonEmptyList(MissingKey(key), Nil)
      case e: ConfigException.WrongType ⇒ NonEmptyList(WrongType(key, classTag[A].toString), Nil)
      case other                        ⇒ NonEmptyList(Underlying(key, other), Nil)
    }

  implicit val yyzReadTypesafeString: ReadValue[Config, String] =
    ReadValue((config, key) ⇒ guard(config.getString(key), key))

  implicit val yyzReadTypesafeInt: ReadValue[Config, Int] =
    ReadValue((config, key) ⇒ guard(config.getInt(key), key))

  implicit def yyzTypesafeNestedRead[A: DecoderV[Config, ?]]: ReadValue[Config, A] =
    ReadValue((config, key) ⇒
      guard(config.getConfig(key), key).andThen(subconfig ⇒
        DecoderV[Config, A].apply(subconfig)
          .leftMap(errors ⇒ NonEmptyList(AtPath(key, errors), Nil))))

}
