/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package classy

import predef._

/** A type class capturing the ability to decode data of type `A` to `B`.
  *
  * To construct a new decoder, consider using [[Decoder.instance]].
  *
  * @groupprio abstract 1
  * @groupname abstract Abstract Value Members
  *
  * @groupprio concrete 2
  * @groupname concrete Concrete Value Members
  *
  * @groupprio config 3
  * @groupname config with Classy Config
  * @groupdesc config Methods available on `Decoder[Config, ?]` when
  * `classy.config._` is imported into scope
  *
  * @groupprio cats 4
  * @groupname cats with Classy Cats
  * @groupdesc cats Methods available when `classy.cats._` is imported
  * into scope
  */
trait Decoder[A, B] extends Serializable {

  /** Decode a value of type `A` as type `B`
    *
    * @group abstract
    */
  def apply(a: A): Either[DecodeError, B]

  import Decoder.instance
  import DecodeError._

  /** Decode a value of type `A` as type `B`.
    *
    * This is an alias for [[apply]].
    *
    * @group concrete
    */
  final def decode(a: A): Either[DecodeError, B] = apply(a)

  /** Construct a new decoder by mapping the output of this decoder
    *
    * @group concrete
    */
  final def map[C](f: B => C): Decoder[A, C] =
    instance(input => apply(input).map(f))

  /** Construct a new decoder by mapping the output of this decoder
    * to either a `DecodeError` or a new result type
    *
    * @group concrete
    */
  final def emap[C](f: B => Either[DecodeError, C]): Decoder[A, C] =
    instance(input => apply(input).flatMap(f))

  /** Construct a new decoder by mapping the error output of this decoder
    * to a new error
    *
    * @group concrete
    */
  final def leftMap(f: DecodeError => DecodeError): Decoder[A, B] =
    instance(input => apply(input).leftMap(f))

  /** Construct a new decoder by mapping the input to this decoder
    *
    * @group concrete
    */
  final def mapInput[Z](f: Z => A): Decoder[Z, B] =
    instance(input => apply(f(input)))

  /** Construct a new decoder through a monadic bind
    *
    * @group concrete
    */
  final def flatMap[C](f: B => Decoder[A, C]): Decoder[A, C] =
    instance(input => apply(input).flatMap(b => f(b).apply(input)))

  /** Compose this decoder with another by using the output of the other
    * decoder as the input to this decoder
    *
    * @see [[compose]]
    *
    * @group concrete
    */
  final def <<<[Z](previous: Decoder[Z, A]): Decoder[Z, B] =
    instance(input => previous.apply(input).flatMap(apply))

  /** Compose this decoder with another by using the output of the other
    * decoder as the input to this decoder
    *
    * @see [[<<<]]
    *
    * @group concrete
    */
  final def compose[Z](previous: Decoder[Z, A]): Decoder[Z, B] =
    this <<< previous

  /** Compose this decoder with another by using the output of this
    * decoder as the input to the other
    *
    * @see [[andThen]]
    *
    * @group concrete
    */
  final def >>>[C](next: Decoder[B, C]): Decoder[A, C] =
    instance(input => apply(input).flatMap(next.apply))

  /** Compose this decoder with another by using the output of this
    * decoder as the input to the other
    *
    * @see [[>>>]]
    *
    * @group concrete
    */
  final def andThen[C](next: Decoder[B, C]): Decoder[A, C] =
    this >>> next

  /** Construct a new decoder by joining this decoder with another,
    * tupling the results. Errors accumulate.
    *
    * @group concrete
    */
  final def and[C](that: Decoder[A, C]): Decoder[A, (B, C)] =
    instance { input =>
      val rb = apply(input)
      val rc = that.apply(input)
      (rb, rc) match {
        case (Right(b), Right(c)) => (b, c).right
        case (Left(eb), Left(ec)) => (eb && ec).left
        case (Left(eb), _)        => eb.left
        case (_, Left(ec))        => ec.left
      }
    }

  /** Construct a new decoder by joining this decoder with another,
    * tupling the results. Errors accumulate.
    *
    * This behaves similar to [[and]] except that instead of always
    * returning a nested tuple it attempts to return a flattened
    * tuple.
    *
    * @group concrete
    *
    * @usecase def join[C](that: Decoder[A, C]): Decoder[A, (B, C)]
    * @inheritdoc
    */
  final def join[C](that: Decoder[A, C])(implicit j: Decoder.Join[B, C]): Decoder[A, j.Out] =
    instance { input =>
      val rb = apply(input)
      val rc = that.apply(input)
      (rb, rc) match {
        case (Right(b), Right(c)) => j(b, c).right
        case (Left(eb), Left(ec)) => (eb && ec).left
        case (Left(eb), _)        => eb.left
        case (_, Left(ec))        => ec.left
      }
    }

  /** Construct a new decoder using this decoder first. If it fails, use
    * the other. Errors accumulate.
    *
    * @group concrete
    */
  final def or[BB >: B](that: Decoder[A, BB]): Decoder[A, BB] =
    instance(input => apply(input) match {
      case b @ Right(_) => b
      case Left(eb) => that.apply(input) match {
        case bb @ Right(_) => bb
        case Left(ebb)     => (eb || ebb).left
      }
    })

  /** Constructs a new decoder that decodes a sequence of inputs into a
    * sequence of outputs.
    *
    * The result is sequenced so that any errors cause the overall
    * decoder to fail. Errors accumulate and are marked with their
    * index if they fail.
    *
    * @group concrete
    *
    * @usecase def sequence: Decoder[List[A], List[B]]
    * @inheritdoc
    */
  final def sequence[F[_]](implicit Ft: Traversable[F], Fi: Indexed[F]): Decoder[F[A], F[B]] =
    instance { fa =>
      val ifa = Fi.indexed(fa)
      val res = Ft.map(ifa)(ia => leftMap(_.atIndex(ia._1)).apply(ia._2))
      Ft.sequence(res)
    }

  /** Constructs a new decoder that optionally decodes a value. Errors
    * other than a missing value still cause the resulting decoder to
    * fail.
    *
    * @group concrete
    */
  final def optional: Decoder[A, Option[B]] =
    instance(input => apply(input).fold(_ match {
      case AtPath(_, Missing) => None.right
      case other              => other.left
    }, _.some.right))

  /** Constructs a new decoder that optionally decodes a value. Deep
    * errors other than a missing value still cause the resulting
    * decoder to fail
    *
    * @group concrete
    *
    * @see [[DecodeError.AtPath.deepError]]
    */
  final def deepOptional: Decoder[A, Option[B]] =
    instance(input => apply(input).fold(_ match {
      case e: AtPath if e.deepError == Missing => None.right
      case other                               => other.left
    }, _.some.right))

  /** Constructs a new decoder that falls back to a default
    * value if a missing value error occurs
    *
    * @group concrete
    */
  final def withDefault(default: B): Decoder[A, B] =
    optional.map(_ getOrElse default)

  /** Constructs a new decoder that falls back to a default
    * value if a deep missing value error occurs
    *
    * @group concrete
    *
    * @see [[DecodeError.AtPath.deepError]]
    */
  final def withDeepDefault(default: B): Decoder[A, B] =
    deepOptional.map(_ getOrElse default)

  /** Constructs a new decoder that falls back to a value if any error
    * occurs
    *
    * To provide default values for a decoder, consider using
    * [[withDefault]].
    *
    * @group concrete
    */
  final def withFallback(fallback: B): Decoder[A, B] =
    instance(input => (apply(input) getOrElse fallback).right)

  /** Construct a new decoder that first reads a path. The value read
    * is then passed to this decoder. Errors are adjusted to reflect
    * that they occurred at a nested path.
    *
    * @group concrete
    */
  final def atPath(path: String)(implicit read: Read[A, A]): Decoder[A, B] =
    read(path) >>> leftMap(_.atPath(path))

  /** Construct a new decoder that traces the result to stdout
    *
    * @group concrete
    */
  final def trace(prefix: String = "> "): Decoder[A, B] =
    instance { input =>
      scala.Predef.println(prefix + input)
      val output = apply(input)
      scala.Predef.println(prefix + output)
      output
    }

  /** Convert a coproduct decoder to a decoder to the least
    * upper bound of the disjunct types
    */
  def lub[B1, B2, BB](implicit
    ev0: B  <:< Either[B1, B2],
    ev1: B1 <:< BB,
    ev2: B2 <:< BB
  ): Decoder[A, BB] = map(_.fold(b1 => b1, b2 => b2))

}

object Decoder {

  /** Implicitly summon a decoder */
  def apply[A, B](implicit ev: Decoder[A, B]): Decoder[A, B] = ev

  /** Construct a new decoder using function `run` for decoding */
  def instance[A, B](run: A => Either[DecodeError, B]): Decoder[A, B] = Instance(run)

  /** The default implementation of [[Decoder]] backed by a function
    * `A => Either[DecodeError, B]`
    *
    * @param run the backing function
    */
  final case class Instance[A, B](run: A => Either[DecodeError, B]) extends Decoder[A, B] {
    override def apply(a: A): Either[DecodeError, B] = run(a)
  }

  /** Construct a decoder that always succeeds with a given value */
  def const[A, B](value: B): Decoder[A, B] = Const(value)

  /** An implementation of [[Decoder]] that decodes a constant
    * succesful value
    *
    * @param value the successful value
    */
  final case class Const[A, B](value: B) extends Decoder[A, B] {
    override def apply(a: A): Either[DecodeError, B] = value.right
  }

  /** Construct a decoder that always fails with the given error */
  def fail[A, B](error: DecodeError): Decoder[A, B] = Fail(error)

  /** An implementation of [[Decoder]] that always fails with constant
    * error
    *
    * @param error the constant error to decode
    */
  final case class Fail[A, B](error: DecodeError) extends Decoder[A, B] {
    override def apply(a: A): Either[DecodeError, B] = error.left
  }

  /** A type class capturing the ability to join two decoders into one
    */
  sealed abstract class Join[A, B] private () extends Serializable {
    type Out
    def apply(a: A, b: B): Out
  }

  /** Companion containing supporting implicits for joining decoders
    * into tupled decoders
    *
    * @groupname helpers Helpers
    *
    * @groupname low Low Arity Instances
    * @groupprio low 10
    */
  object Join extends JoinInstances0 {

    /** @group helpers */
    type Aux[A, B, C] = Join[A, B] { type Out = C }

    /** Create a [[Join]] instance with [[Join.Out]] equal to `C`
      *
      * @group helpers
      */
    def instance[A, B, C](f: (A, B) => C): Join.Aux[A, B, C] =
      new Join[A, B] {
        type Out = C
        override def apply(a: A, b: B): Out = f(a, b)
      }
  }

  private[Decoder] sealed trait JoinInstances0 extends JoinInstances1 { self: Join.type =>

    /** Join a `Tuple1` on the left and a single value on the right into a `Tuple2`
      *
      * @group low
      */
    implicit def join1_0[A0, B0]: Join.Aux[Tuple1[A0], B0, (A0, B0)] =
      instance((a, b) => (a._1, b))

    /** Join a `Tuple2` on the left and a single value on the right into a `Tuple3`
      *
      * @group low
      */
    implicit def join2_0[A0, A1, B0]: Join.Aux[(A0, A1), B0, (A0, A1, B0)] =
      instance((a, b) => (a._1, a._2, b))

    /** Join a `Tuple3` on the left and a single value on the right into a `Tuple4`
      *
      * @group low
      */
    implicit def join3_0[A0, A1, A2, B0]: Join.Aux[(A0, A1, A2), B0, (A0, A1, A2, B0)] =
      instance((a, b) => (a._1, a._2, a._3, b))

    implicit def join4_0[A0, A1, A2, A3, B0]: Join.Aux[(A0, A1, A2, A3), B0, (A0, A1, A2, A3, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, b))
    implicit def join5_0[A0, A1, A2, A3, A4, B0]: Join.Aux[(A0, A1, A2, A3, A4), B0, (A0, A1, A2, A3, A4, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, b))
    implicit def join6_0[A0, A1, A2, A3, A4, A5, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5), B0, (A0, A1, A2, A3, A4, A5, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, b))
    implicit def join7_0[A0, A1, A2, A3, A4, A5, A6, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6), B0, (A0, A1, A2, A3, A4, A5, A6, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, b))
    implicit def join8_0[A0, A1, A2, A3, A4, A5, A6, A7, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7), B0, (A0, A1, A2, A3, A4, A5, A6, A7, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, b))
    implicit def join9_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, b))
    implicit def join10_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, b))
    implicit def join11_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, b))
    implicit def join12_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, b))
    implicit def join13_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, b))
    implicit def join14_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, b))
    implicit def join15_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15, b))
    implicit def join16_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15, a._16, b))
    implicit def join17_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15, a._16, a._17, b))
    implicit def join18_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15, a._16, a._17, a._18, b))
    implicit def join19_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15, a._16, a._17, a._18, a._19, b))
    implicit def join20_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15, a._16, a._17, a._18, a._19, a._20, b))
    implicit def join21_0[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, B0]: Join.Aux[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20), B0, (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, B0)] =
      instance((a, b) => (a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15, a._16, a._17, a._18, a._19, a._20, a._21, b))

  }

  private[Decoder] sealed trait JoinInstances1 { self: Join.type =>

    /** Join a single value on the left and a single value on the right into a `Tuple2`
      *
      * @group low
      */
    implicit def join0_0[A0, B0]: Join.Aux[A0, B0, (A0, B0)] =
      instance((a, b) => (a, b))
  }

}
