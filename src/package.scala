/* -
 * Case Classy [case-classy]
 */

import cats.data._
import cats.Monad
import cats.Show

package object classy extends ClassyDerivation with ClassyDefaultInstances {

  private[classy]type ResV[A] = ValidatedNel[DecodeError, A]
  private type Res[A] = Xor[RootDecodeError, A]

  /** DecoderV
    * A very thin wrapper around `C ⇒ ResV[A]`.
    */
  case class DecoderV[C, A](run: C ⇒ ResV[A]) extends (C ⇒ ResV[A]) {
    def apply(source: C): ResV[A] = run(source)
  }
  object DecoderV {
    def apply[C, A](implicit ev: DecoderV[C, A]): DecoderV[C, A] = ev
  }

  case class Decoder[C, A](decoderV: DecoderV[C, A]) extends (C ⇒ Res[A]) {
    def apply(source: C): Res[A] =
      decoderV(source).toXor.leftMap(RootDecodeError.apply)
  }

  object Decoder {
    def apply[C, A](implicit ev: Decoder[C, A]): Decoder[C, A] = ev
    implicit def fromDecoderV[C, A](decoder: DecoderV[C, A]): Decoder[C, A] =
      Decoder(decoder)
  }

  /** ReadValue
    * A very thin wrapper around `(C, String) ⇒ Res[A]`.
    */
  case class ReadValue[C, A](
      run: (C, String) ⇒ ResV[A]
  ) extends ((C, String) ⇒ ResV[A]) {
    def apply(config: C, key: String): ResV[A] = run(config, key)
  }
  object ReadValue {
    def apply[C, A](implicit ev: ReadValue[C, A]): ReadValue[C, A] = ev
  }

  final case class RootDecodeError(errors: NonEmptyList[DecodeError]) extends Throwable {
    final override def fillInStackTrace(): Throwable = this
    def show: String = Show[RootDecodeError].show(this)
  }
  object RootDecodeError {
    implicit def showRootDecodeError: Show[RootDecodeError] = Show.show(z ⇒
      Monad[NonEmptyList].flatMap(z.errors)(_.flatten).map(_.show).toList.mkString(", "))
    //_.errors.unwrap.map(_.show).mkString(", "))
  }

  sealed abstract class DecodeError extends Throwable {
    final override def fillInStackTrace(): Throwable = this
    def key: String
    def show: String = DecodeError.show(this)
    def flatten(): NonEmptyList[DecodeError] = NonEmptyList(this, Nil)
    override def toString(): String =
      s"${getClass.getName}($show)"
  }

  object DecodeError {
    case class MissingKey(key: String) extends DecodeError
    case class WrongType(key: String, expected: String) extends DecodeError
    case class AtPath(key: String, errors: NonEmptyList[DecodeError])
        extends DecodeError { parent ⇒
      override def flatten(): NonEmptyList[DecodeError] =
        Monad[NonEmptyList].flatMap(errors) {
          case e: MissingKey ⇒ e.copy(key = s"${parent.key}.${e.key}").flatten()
          case e: WrongType  ⇒ e.copy(key = s"${parent.key}.${e.key}").flatten()
          case e: AtPath     ⇒ e.copy(key = s"${parent.key}.${e.key}").flatten()
          case e: BadFormat  ⇒ e.copy(key = s"${parent.key}.${e.key}").flatten()
          case e: Underlying ⇒ e.copy(key = s"${parent.key}.${e.key}").flatten()
        }
    }
    case class BadFormat(key: String, message: String) extends DecodeError
    case class Underlying(key: String, underlying: Throwable) extends DecodeError

    implicit val showDecodeError: Show[DecodeError] = Show.show[DecodeError] {
      case MissingKey(key)          ⇒ s"Missing value for $key"
      case WrongType(key, expected) ⇒ s"Wrong type for $key, expected $expected"
      case AtPath(key, errors)      ⇒ s"$errors at $key"
      case BadFormat(key, message)  ⇒ s"Bad value format for $key: $message"
      case Underlying(key, t)       ⇒ s"Underlying exception for key $key: $t"
    }

    def show(error: DecodeError): String = showDecodeError.show(error)
  }
}
