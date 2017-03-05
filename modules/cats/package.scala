/* -
 * Case Classy [classy-cats]
 */

package classy

import _root_.cats._
import _root_.cats.data.Kleisli

import core._

package object cats {

  implicit class DecoderCatsOps[A, B](
      private val decoder: Decoder[A, B]
  ) extends AnyVal {

    /** A Kleisli arrow for this decoder
      *
      * @group cats
      */
    def kleisli: Kleisli[({ type λ[α] = Either[DecodeError, α] })#λ, A, B] =
      Kleisli(decoder.apply)

    // Note: Kind projector syntax isn't used above, as it produces a dirtier
    // unidoc unless used in conjuction with a scaladoc @usecase
  }

  implicit def decoderMonadInstance[Z]: Monad[Decoder[Z, ?]] =
    new Monad[Decoder[Z, ?]] {

      override def map[A, B](a: Decoder[Z, A])(f: A => B): Decoder[Z, B] =
        a.map(f)

      def pure[A](x: A): Decoder[Z, A] =
        Decoder.const(x)

      def flatMap[A, B](fa: Decoder[Z, A])(f: A => Decoder[Z, B]): Decoder[Z, B] =
        fa.flatMap(f)

      // Currently *not* stack safe
      def tailRecM[A, B](a: A)(f: A => Decoder[Z, Either[A, B]]): Decoder[Z, B] =
        f(a).flatMap {
          case Left(a1) => tailRecM(a1)(f)
          case Right(b) => Decoder.const(b)
        }
    }

  implicit val decodeErrorEq: Eq[DecodeError] =
    new Eq[DecodeError] {
      def eqv(x: DecodeError, y: DecodeError): Boolean = x == y
    }

  object DecodeErrorMonoid {
    object and {
      implicit val decodeErrorMonoidAnd: Monoid[DecodeError] = new Monoid[DecodeError] {
        def empty = DecodeError.Identity
        def combine(x: DecodeError, y: DecodeError): DecodeError = x && y
      }
    }
    object or {
      implicit val decodeErrorMonoidOr: Monoid[DecodeError] = new Monoid[DecodeError] {
        def empty = DecodeError.Identity
        def combine(x: DecodeError, y: DecodeError): DecodeError = x || y
      }
    }
  }

}
