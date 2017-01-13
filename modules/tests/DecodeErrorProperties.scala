/* -
 * Case Classy [classy-tests]
 */

package classy
package core

import scala.Predef._

import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.listOf
import org.scalacheck.Prop._

import org.scalacheck.derive._
import org.scalacheck.{ Shapeless => blackMagic }

class DecodeErrorProperties extends Properties("DecodeError") {
  import DecodeError._

  implicit val genLeaf: Gen[DecodeError] = {
    import blackMagic._
    MkArbitrary[LeafDecodeError].arbitrary.arbitrary
  }

  property("and two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => DecodeError.and(a, b) ?= And(a, b))

  property("&& two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => a && b ?= And(a, b))

  property("or two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => DecodeError.or(a, b) ?= Or(a, b))

  property("|| two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => a || b ?= Or(a, b))

  property("and many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(DecodeError.and) ?= And(errors.head, errors.tail)))

  property("&& many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(_ && _) ?= And(errors.head, errors.tail)))

  property("or many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(DecodeError.or) ?= Or(errors.head, errors.tail)))

  property("|| many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(_ || _) ?= Or(errors.head, errors.tail)))

  property("atPath") =
    forAll(
      arbitrary[String] :| "path",
      arbitrary[String] :| "missing path"
    )((path, missingPath) =>
        Missing.atPath(missingPath).atPath(path) ?=
          AtPath(path, AtPath(missingPath, Missing)))

  property("atIndex") =
    forAll(
      arbitrary[Int] :| "index",
      arbitrary[String] :| "missing path"
    )((index, missingPath) =>
        Missing.atPath(missingPath).atIndex(index) ?=
          AtIndex(index, AtPath(missingPath, Missing)))
}
