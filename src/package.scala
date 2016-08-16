/* -
 * Case Classy [case-classy]
 */

import cats.data._

package object classy extends DefaultInstances {

  private[classy]type Res[A] = ValidatedNel[DecodeError, A]

  /** Decoder
    * A very thin wrapper around `C ⇒ Res[A]`.
    */
  case class Decoder[C, A](run: C ⇒ Res[A]) extends (C ⇒ Res[A]) {
    def apply(source: C): Res[A] = run(source)
  }
  object Decoder {
    def apply[C, A](implicit ev: Decoder[C, A]): Decoder[C, A] = ev
  }

  /** ReadValue
    * A very thin wrapper around `(C, String) ⇒ Res[A]`.
    */
  case class ReadValue[C, A](
      run: (C, String) ⇒ Res[A]
  ) extends ((C, String) ⇒ Res[A]) {
    def apply(config: C, key: String): Res[A] = run(config, key)
  }
  object ReadValue {
    def apply[C, A](implicit ev: ReadValue[C, A]): ReadValue[C, A] = ev
  }

  sealed trait DecodeError extends Product with Serializable
  object DecodeError {
    case class MissingKey(key: String) extends DecodeError
    case class WrongType(key: String) extends DecodeError
    case class AtPath(key: String, errors: NonEmptyList[DecodeError])
      extends DecodeError
    case class BadFormat(key: String, message: String) extends DecodeError
  }

}
