/* -
 * Case Classy [case-classy-tests]
 */

package classy
package core

import scala.Predef._

import org.scalacheck._
import org.scalacheck.Prop._

object DecoderProperties extends {

  def checkAnd[A, B, C](
    implicit
    evA: Arbitrary[A],
    evAB: Arbitrary[Decoder[A, B]],
    evAC: Arbitrary[Decoder[A, C]]
  ): Prop = forAll { (a: A, dab: Decoder[A, B], dac: Decoder[A, C]) ⇒

    (dab and dac).decode(a) match {
      case Right((b, c)) ⇒
        (dab.decode(a) ?= b.right) && (dac.decode(a) ?= c.right)
      case Left(e) ⇒
        ???
    }

  }

}
