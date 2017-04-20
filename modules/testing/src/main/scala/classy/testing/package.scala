/* -
 * Case Classy [classy-testing]
 */

package classy

import _root_.cats._
import org.scalacheck._
//import org.scalacheck.Gen._

//import scala.concurrent.duration.Duration
//import scala.concurrent.duration.FiniteDuration
import java.util.UUID

package object testing {

  implicit lazy val arbDecodeError: Arbitrary[DecodeError] = {
    import org.scalacheck.derive._
    import org.scalacheck.Shapeless._
    MkArbitrary[DecodeError].arbitrary
  }

  implicit def decoderEq[A, B](implicit ArbA: Arbitrary[A]): Eq[Decoder[A, B]] =
    new Eq[Decoder[A, B]] {
      def eqv(x: Decoder[A, B], y: Decoder[A, B]): Boolean = {
        val a = ArbA.arbitrary.sample.get
        x(a) == y(a)
      }
    }

  implicit lazy val arbUUID: Arbitrary[UUID] = Arbitrary(Gen.uuid)
  implicit lazy val cogenUUID: Cogen[UUID] = Cogen(_.getMostSignificantBits)

  /*
  lazy val genFiniteDuration: Gen[FiniteDuration] =
    chooseNum(Long.MinValue + 1, Long.MaxValue).map(Duration.fromNanos)

  lazy val genDuration: Gen[Duration] = frequency(
    (1, const(Duration.Inf)),
    (1, const(Duration.MinusInf)),
    (1, const(Duration.Undefined)),
    (1, const(Duration.Zero)),
    (6, genFiniteDuration))

  implicit lazy val arbFiniteDuration: Arbitrary[FiniteDuration] =
    Arbitrary(genFiniteDuration)
  implicit lazy val cogenFiniteDuration: Cogen[FiniteDuration] =
    Cogen(_.length)
  implicit lazy val arbDuration: Arbitrary[Duration] =
    Arbitrary(genDuration)
  implicit lazy val cogenDuration: Cogen[Duration] =
    Cogen(_.length)
  */

}
