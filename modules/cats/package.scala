/* -
 * Case Classy [classy-cats]
 */

package classy

import _root_.cats._
import _root_.cats.data.Kleisli
import _root_.cats.instances.either._
import _root_.cats.syntax.either._
import scala.annotation.tailrec

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

  implicit def decoderMonadErrorInstance[Z]: MonadError[Decoder[Z, ?], DecodeError] =
    new MonadError[Decoder[Z, ?], DecodeError] {

      override def map[A, B](a: Decoder[Z, A])(f: A => B): Decoder[Z, B] =
        a.map(f)

      def pure[A](x: A): Decoder[Z, A] =
        Decoder.const(x)

      def flatMap[A, B](fa: Decoder[Z, A])(f: A => Decoder[Z, B]): Decoder[Z, B] =
        fa.flatMap(f)

      final def tailRecM[A, B](a: A)(f: A => Decoder[Z, Either[A, B]]): Decoder[Z, B] =
        new Decoder[Z, B] {
          @tailrec
          private[this] def step(a1: A, z: Z): Either[DecodeError, B] = f(a1)(z) match {
            case l @ Left(_) => l.rightCast[B]
            case Right(x) =>
              x match {
                case Left(a2) => step(a2, z)
                case r @ Right(_) => r.leftCast[DecodeError]
              }
          }

          final def apply(z: Z): Either[DecodeError, B] = step(a, z)
        }

      final def raiseError[A](e: DecodeError): Decoder[Z, A] =
        Decoder.fail(e)

      final def handleErrorWith[A](fa: Decoder[Z, A])(f: DecodeError => Decoder[Z, A]): Decoder[Z, A] =
        Decoder.instance { z =>
          MonadError[Either[DecodeError, ?], DecodeError].handleErrorWith(fa.apply(z))(err => f(err)(z))
        }
    }

  implicit def decoderSemigroupKInstance[Z]: SemigroupK[Decoder[Z, ?]] =
    new SemigroupK[Decoder[Z, ?]] {
      def combineK[A](d1: Decoder[Z, A], d2: Decoder[Z, A]): Decoder[Z, A] =
        d1 or d2
    }

  implicit val decodeErrorEq: Eq[DecodeError] =
    Eq.fromUniversalEquals

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
