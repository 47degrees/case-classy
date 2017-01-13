/* -
 * Case Classy [classy-core]
 */

package classy
package core

import scala.util.{ Either, Right }

import wheel._

/** A type class capturing the ability to decode data of type `A` to `B`.
  *
  * To construct a new decoder, consider using [[Decoder.instance]].
  */
trait Decoder[A, B] extends Serializable {

  /** Decode a value of type `A` as type `B` */
  def decode(a: A): Either[DecodeError, B]

  import Decoder.instance
  import DecodeError._

  /** Construct a new decoder by mapping the output of this decoder
    */
  final def map[C](f: B => C): Decoder[A, C] =
    instance(input => decode(input).map(f))

  /** Construct a new decoder by mapping the output of this decoder
    * to either a `DecodeError` or a new result type
    */
  final def emap[C](f: B => Either[DecodeError, C]): Decoder[A, C] =
    instance(input => decode(input).flatMap(f))

  /** Construct a new decoder by mapping the error output of this decoder
    * to a new error
    */
  final def leftMap(f: DecodeError => DecodeError): Decoder[A, B] =
    instance(input => decode(input).leftMap(f))

  /** Construct a new decoder by mapping the input to this decoder
    */
  final def mapInput[Z](f: Z => A): Decoder[Z, B] =
    instance(input => decode(f(input)))

  /** Construct a new decoder through a monadic bind */
  final def flatMap[C](f: B => Decoder[A, C]): Decoder[A, C] =
    instance(input => decode(input).flatMap(b => f(b).decode(input)))

  /** Construct a new decoder by using the result of another decoder as
    * the input to this decoder
    */
  final def compose[Z](previous: Decoder[Z, A]): Decoder[Z, B] =
    instance(input => previous.decode(input).flatMap(decode))

  /** Construct a new decoder by using the output of this decoder as
    * the input of another
    */
  final def andThen[C](next: Decoder[B, C]): Decoder[A, C] =
    instance(input => decode(input).flatMap(next.decode))

  /** Construct a new decoder by joining this decoder with another,
    * tupling the results. Errors accumulate.
    */
  final def and[C](that: Decoder[A, C]): Decoder[A, (B, C)] =
    instance { input =>
      val rb = decode(input)
      val rc = that.decode(input)
      (rb, rc) match {
        case (Right(b), Right(c)) => (b, c).right
        case (Left(eb), Left(ec)) => (eb && ec).left
        case (Left(eb), _)        => eb.left
        case (_, Left(ec))        => ec.left
      }
    }

  /** Construct a new decoder using this decoder first. If it fails, use
    * the other. Errors accumulate.
    */
  final def or[BB >: B](that: Decoder[A, BB]): Decoder[A, BB] =
    instance(input => decode(input) match {
      case b @ Right(_) => b
      case Left(eb) => that.decode(input) match {
        case bb @ Right(_) => bb
        case Left(ebb)     => (eb || ebb).left
      }
    })

  /** Constructs a new decoder that decodes a sequence of inputs. The
    * result is sequenced so that any errors cause the overall decoder
    * to fail. Errors accumulate and are marked with their index if
    * they fail.
    */
  final def sequence[F[_]](implicit Ft: Traversable[F], Fi: Indexed[F]): Decoder[F[A], F[B]] =
    instance { fa =>
      val ifa = Fi.indexed(fa)
      val res = Ft.map(ifa)(ia => leftMap(_.atIndex(ia._1)).decode(ia._2))
      Ft.sequence(res)
    }

  /** Constructs a new decoder that optionally decodes a value. Errors
    * other than a missing value still cause the resulting decoder to
    * fail.
    */
  final def optional: Decoder[A, Option[B]] =
    instance(input => decode(input).fold(_ match {
      case AtPath(_, Missing) => None.right
      case other              => other.left
    }, _.some.right))

  /** Constructs a new decoder that falls back to a default
    * value if a missing value error occurs.
    */
  final def withDefault(default: B): Decoder[A, B] =
    optional.map(_ getOrElse default)

  /** Constructs a new decoder that falls back to a value if _any_ error
    * occurs.
    *
    * To provide default values for a decoder, consider using
    * [[withDefault]].
    */
  final def withFallback(fallback: B): Decoder[A, B] =
    instance(input => (decode(input) getOrElse fallback).right)

  /** Construct a new decoder that first reads a path. The value read
    * is then passed to this decoder. Errors are adjusted to reflect
    * that they occurred at a nested path.
    */
  final def atPath(path: String)(implicit read: Read[A, A]): Decoder[A, B] =
    read(path) andThen leftMap(_.atPath(path))

  /** Construct a new decoder that traces the result to stdout
    */
  final def trace(prefix: String = "> "): Decoder[A, B] =
    instance { input =>
      scala.Predef.println(prefix + input)
      val output = decode(input)
      scala.Predef.println(prefix + output)
      output
    }

}

object Decoder {
  /** Implicitly summon a decoder */
  def apply[A, B](implicit ev: Decoder[A, B]): Decoder[A, B] = ev

  /** Construct a new decoder using function `f` for decoding */
  def instance[A, B](
    run: A => Either[DecodeError, B]): Decoder[A, B] = Instance(run)

  case class Instance[A, B](run: A => Either[DecodeError, B]) extends Decoder[A, B] {
    override def decode(a: A): Either[DecodeError, B] = run(a)
  }

  /** Construct a decoder that always succeeds with a given value */
  def const[A, B](value: B): Decoder[A, B] = instance(_ => value.right)

  /** Construct a decoder that always fails with the given error */
  def fail[A, B](error: DecodeError): Decoder[A, B] = instance(_ => error.left)
}
