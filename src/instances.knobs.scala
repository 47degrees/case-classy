/* -
 * Case Classy [case-classy-knobs]
 */

package classy

import cats.data._
import _root_.knobs._

import scala.reflect.{ classTag, ClassTag }

object knobs {
  import DecodeError._

  type KnobsDecoder[A] = Decoder[Config, A]
  object KnobsDecoder {
    def apply[A](implicit ev: KnobsDecoder[A]): KnobsDecoder[A] = ev
  }

  def deriveKnobsDecoder[A: KnobsDecoder] = Decoder[Config, A]

  implicit def yyzReadKnobsSupport[A: Configured: ClassTag]: ReadValue[Config, A] =
    ReadValue((config, key) ⇒
      Xor.fromOption(config.env.get(key), MissingKey(key))
        .flatMap(value ⇒ Xor.fromOption(
          value.convertTo[A],
          WrongType(key, classTag[A].toString)))
        .toValidatedNel)

  implicit def yyzKnobsNestedReadSupport[A: DecoderV[Config, ?]]: ReadValue[Config, A] =
    ReadValue((config, key) ⇒
      DecoderV[Config, A].apply(config.subconfig(key))
        .leftMap(errors ⇒ NonEmptyList(AtPath(key, errors))))
}
