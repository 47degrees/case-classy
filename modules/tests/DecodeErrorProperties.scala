/* -
 * Case Classy [case-classy-tests]
 */

package classy
package core

import scala.Predef._

import org.scalacheck._
import org.scalacheck.Prop._

import org.scalacheck.derive._
import org.scalacheck.{ Shapeless => blackMagic }

class DecodeErrorProperties extends Properties("DecodeError") {
  import DecodeError._

  // TODO: This needs work. Maybe just use Cat's laws and some type class instances
  // to prove good behavior

  implicit val arbitraryLeafDecodeError: Arbitrary[LeafDecodeError] = {
    import blackMagic._
    MkArbitrary[LeafDecodeError].arbitrary
  }

  property("combine two leaf errors") = forAll { (a: LeafDecodeError, b: LeafDecodeError) =>
    DecodeError.combine(a, b) ?= Aggregate(a, b :: Nil)
  }

  val atLeast2Leafs = Gen.listOf(arbitraryLeafDecodeError.arbitrary) suchThat (_.length >= 2)
  property("combine many leaf errors") = forAll(atLeast2Leafs) { errors =>
    (errors: List[DecodeError]).reduce(_ && _) ?= Aggregate(errors.head, errors.tail)
  }

}
