/* -
 * Case Classy [case-classy-core]
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

  /** Construct a new decober by joining this decoder with another,
    * tupling the results
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
    * the other.
    */
  final def or[BB >: B](that: Decoder[A, BB]): Decoder[A, BB] =
    instance(input => decode(input) match {
      case right @ Right(_) => right
      case _                => that.decode(input)
    })

  /** Constructs a new decoder that decodes a sequence of inputs. The
    * result is sequenced so that any errors cause the overall decoder
    * to fail.
    */
  final def sequence[F[_]](implicit F: Traversable[F]): Decoder[F[A], F[B]] =
    instance(fa => F.sequence(F.map(fa)(decode)))

  /** Constructs a new decoder that optionally decodes a value. Errors
    * other than a missing value still cause the resulting decoder to
    * fail.
    */
  final def optional: Decoder[A, Option[B]] =
    instance(input => decode(input).fold(_ match {
      case _: DecodeError.MissingKey => None.right
      case other                     => other.left
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
    * is then passed to this decoder
    */
  final def atPath(path: String)(implicit read: Read[A, A]): Decoder[A, B] =
    this compose read(path)

}

object Decoder {

  /** Implicitly summon a decoder */
  def apply[A, B](implicit ev: Decoder[A, B]): Decoder[A, B] = ev

  /** Construct a new decoder using function `f` for decoding */
  def instance[A, B](f: A => Either[DecodeError, B]): Decoder[A, B] = new Decoder[A, B] {
    override def decode(a: A): Either[DecodeError, B] = f(a)
  }

  /** Construct a decoder that always succeeds with a given value */
  def const[A, B](value: B): Decoder[A, B] = instance(_ => value.right)

  /** Construct a decoder that always fails with the given error */
  def fail[A, B](error: DecodeError): Decoder[A, B] = instance(_ => error.left)
}
