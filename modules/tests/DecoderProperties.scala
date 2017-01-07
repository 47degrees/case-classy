/* -
 * Case Classy [classy-tests]
 */

package classy
package core

import _root_.cats.instances.all._
import _root_.cats.laws.discipline._
import org.scalacheck._

import testing._
import cats._

class DecoderProperties extends Properties("Decoder") {

  implicit def decoder[A, B](implicit
    ArbF: Arbitrary[A => Either[DecodeError, B]],
    ArbDecodeError: Arbitrary[DecodeError],
    ArbDAB: Arbitrary[B]): Arbitrary[Decoder[A, B]] = Arbitrary(
    Gen.oneOf(
      ArbF.arbitrary.map(f => Decoder.instance(f)),
      ArbDAB.arbitrary.map(value => Decoder.const[A, B](value)),
      ArbDecodeError.arbitrary.map(value => Decoder.fail[A, B](value))))

  include(
    DecoderChecks.positive(Decoder.instance[String, String](v => v.right))(v => v),
    "identity ")

  include(DecoderChecks.positive(
    (result: String) => ("", Decoder.const[String, String](result))))

  include(MonadTests[Decoder[String, ?]].stackUnsafeMonad[String, String, String].all)

}
