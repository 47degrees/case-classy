/* -
 * Case Classy [classy-testing]
 */

package classy

import _root_.cats._
import org.scalacheck._

import core._

package object testing {

  implicit val arbDecodeError: Arbitrary[DecodeError] = {
    import org.scalacheck.derive._
    import org.scalacheck.Shapeless._
    MkArbitrary[DecodeError].arbitrary
  }

  implicit def decoderEq[A, B](implicit ArbA: Arbitrary[A]): Eq[Decoder[A, B]] =
    new Eq[Decoder[A, B]] {
      def eqv(x: Decoder[A, B], y: Decoder[A, B]): Boolean = {
        val a = ArbA.arbitrary.sample.get
        x.decode(a) == y.decode(a)
      }
    }

}
