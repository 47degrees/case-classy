/* -
 * Case Classy [classy-testing]
 */

package classy
package testing

import scala.Predef._

import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._

import core._

object DecoderChecks {
  import DecodeError._

  def checkDecodeSuccess[A, B](
    dab: Decoder[A, B],
    a: A,
    b: B
  ): Prop =
    dab(a) ?= b.right

  def checkMap[A, B, C](
    dab: Decoder[A, B],
    a: A,
    fbc: B => C
  ): Prop =
    dab(a).map(fbc) ?= dab.map(fbc)(a)

  def checkOptional[A, B](
    dab: Decoder[A, B],
    a: A
  ): Prop =
    dab(a) match {
      case Left(AtPath(_, Missing)) => dab.optional(a) ?= None.right
      case Left(e)                  => dab.optional(a) ?= e.left
      case Right(b)                 => dab.optional(a) ?= b.some.right
    }

  def checkDeepOptional[A, B](
    dab: Decoder[A, B],
    a: A
  ): Prop =
    dab(a) match {
      case Left(e: AtPath) if e.deepError == Missing => dab.deepOptional(a) ?= None.right
      case Left(e)                                   => dab.deepOptional(a) ?= e.left
      case Right(b)                                  => dab.deepOptional(a) ?= b.some.right
    }

  def checkWithDefault[A, B](
    dab: Decoder[A, B],
    a: A,
    b0: B
  ): Prop =
    dab(a) match {
      case Left(AtPath(_, Missing)) => dab.withDefault(b0)(a) ?= b0.right
      case Left(e)                  => dab.withDefault(b0)(a) ?= e.left
      case Right(b)                 => dab.withDefault(b0)(a) ?= b.right
    }

  def checkWithDeepDefault[A, B](
    dab: Decoder[A, B],
    a: A,
    b0: B
  ): Prop =
    dab(a) match {
      case Left(e: AtPath) if e.deepError == Missing => dab.withDeepDefault(b0)(a) ?= b0.right
      case Left(e)                                   => dab.withDeepDefault(b0)(a) ?= e.left
      case Right(b)                                  => dab.withDeepDefault(b0)(a) ?= b.right
    }

  def checkOptionalDefaultConsistency[A, B](
    dab: Decoder[A, B],
    a: A,
    b0: B
  ): Prop = dab.optional(a).map(_ getOrElse b0) ?= dab.withDefault(b0)(a)

  def checkDeepOptionalDeepDefaultConsistency[A, B](
    dab: Decoder[A, B],
    a: A,
    b0: B
  ): Prop = dab.deepOptional(a).map(_ getOrElse b0) ?= dab.withDeepDefault(b0)(a)

  def checkAnd[A, B, C](
    dab: Decoder[A, B],
    dac: Decoder[A, C],
    a: A
  ): Prop = ((dab and dac)(a), dab(a), dac(a)) match {
    case (Right((b, c)), db, dc)       => (b.right ?= db) && (c.right ?= dc)
    case (Left(e), Left(eb), Left(ec)) => e ?= (eb && ec)
    case (Left(e), _, Left(ec))        => e ?= ec
    case (Left(e), Left(eb), _)        => e ?= eb
    case (Left(e), Right(b), Right(c)) => falsified :| "and failed but individual decoders passed"
  }

  private def properties[A](name: String)(f: Properties => A): Properties = {
    val res = new Properties(name)
    f(res)
    res
  }

  def positive[A: Arbitrary, B: Arbitrary](
    decoder: Decoder[A, B])(fba: B => A)(implicit ArbBtoB: Arbitrary[B => B], readAtoA: Read[A, A]): Properties =
    positive[A, B]((b: B) => (fba(b), decoder))

  def positive[A: Arbitrary, B: Arbitrary](
    make: B => (A, Decoder[A, B])
  )(implicit ArbBtoB: Arbitrary[B => B], readAtoA: Read[A, A]): Properties = properties("operations") { self =>
    import self._

    // format: OFF

    property("decode") = forAll(
      "result"  |: arbitrary[B]
    ) { (b) =>
      val (a, dab) = make(b)
      checkDecodeSuccess(dab, a, b)
    }

    property("map") = forAll(
      "result"  |: arbitrary[B],
      "mapper"  |: arbitrary[B => B]
    ) { (b, fbc) =>
      val (a, dab) = make(b)
      checkMap(dab, a, fbc)
    }

    property("optional") = forAll(
      "decoder seed" |: arbitrary[B],
      "bad input"    |: arbitrary[A]
    ) { (b, na) =>
      val (a, dab) = make(b)
      a != na ==> checkOptional(dab, na)
    }

    property("deepOptional") = forAll(
      "decoder seed" |: arbitrary[B],
      "bad input"    |: arbitrary[A],
      "paths"        |: arbitrary[List[String]]
    ) { (b, na, paths) =>
      val (a, dab) = make(b)
      a != na ==> checkDeepOptional(paths.foldLeft(dab)(_ atPath _), na)
    }

    property("withDefault") = forAll(
      "decoder seed" |: arbitrary[B],
      "bad input"    |: arbitrary[A],
      "default"      |: arbitrary[B]
    ) { (sb, na, db) =>
      val (a, dab) = make(sb)
      a != na ==> checkWithDefault(dab, na, db)
    }

    property("withDeepDefault") = forAll(
      "decoder seed" |: arbitrary[B],
      "bad input"    |: arbitrary[A],
      "default"      |: arbitrary[B],
      "paths"        |: arbitrary[List[String]]
    ) { (sb, na, db, paths) =>
      val (a, dab) = make(sb)
      a != na ==> checkWithDeepDefault(paths.foldLeft(dab)(_ atPath _), na, db)
    }

    property("optional/withDefault consistency") =forAll(
      "result"    |: arbitrary[B],
      "bad input" |: arbitrary[A],
      "default"   |: arbitrary[B]
    ) { (b, na, db) =>
      val (a, dab) = make(b)
      a != na ==> checkOptionalDefaultConsistency(dab, a, b)
    }

    property("deepOptional/withDeepDefault consistency") =forAll(
      "result"    |: arbitrary[B],
      "bad input" |: arbitrary[A],
      "default"   |: arbitrary[B],
      "paths"     |: arbitrary[List[String]]
    ) { (b, na, db, paths) =>
      val (a, dab) = make(b)
      a != na ==> checkDeepOptionalDeepDefaultConsistency(
        paths.foldLeft(dab)(_ atPath _), a, b)
    }

    property("and") = forAll(
      "decoder 1 seed" |: arbitrary[B],
      "decoder 2 seed" |: arbitrary[B],
      "input"          |: arbitrary[A]
    ) { (sb1, sb2, a) =>
      val (_, dab1) = make(sb1)
      val (_, dab2) = make(sb2)
      checkAnd(dab1, dab2, a)
    }

    // format: ON

  }

}
