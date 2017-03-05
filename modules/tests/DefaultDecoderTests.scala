/* -
 * Case Classy [classy-tests]
 */

package classy
package core

import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._

import shapeless.Typeable

import scala.Predef._
import java.util.UUID

object DefaultChecks {

  implicit class DecoderCheckOps[A, B](val decoder: Decoder[A, B]) extends AnyVal {
    def succeeds(input: A, output: B): Prop = decoder(input) ?= output.right
    def failsWrongType(input: A)(implicit ev: Typeable[B]): Prop =
      decoder(input) ?= DecodeError.WrongType(ev.describe, input.toString.some).left
  }

  def section[A, B](decoder: Decoder[A, B], f: B => A = (_: B).toString)(implicit arbB: Arbitrary[B]): Prop =
    forAll(arbB.arbitrary :| "input")((output) =>
      decoder.succeeds(f(output), output))

  def retraction[A: Arbitrary, B: Typeable](decoder: Decoder[A, B])(f: A => Option[B]): Prop =
    forAll(arbitrary[A] :| "input")((input) =>
      f(input).fold(
        decoder.failsWrongType(input))(
        decoder.succeeds(input, _)))

  def catching[A, B](f: A => B): A => Option[B] =
    (a: A) => scala.util.Try(f(a)).toOption

  def stringToBooleanProperties(decoder: Decoder[String, Boolean]): Properties =
    new Properties("String -> Boolean") {
      property("section String") = section(decoder)
      property("retract Boolean") = retraction(decoder)(catching(_.toBoolean))
    }

  def stringToByteProperties(decoder: Decoder[String, Byte]): Properties =
    new Properties("String -> Byte") {
      property("section String") = section(decoder)
      property("retract Byte") = retraction(decoder)(catching(_.toByte))
    }

  def stringToShortProperties(decoder: Decoder[String, Short]): Properties =
    new Properties("String -> Short") {
      property("section String") = section(decoder)
      property("retract Short") = retraction(decoder)(catching(_.toShort))
    }

  def stringToIntProperties(decoder: Decoder[String, Int]): Properties =
    new Properties("String -> Int") {
      property("section String") = section(decoder)
      property("retract Int") = retraction(decoder)(catching(_.toInt))
    }

  def stringToLongProperties(decoder: Decoder[String, Long]): Properties =
    new Properties("String -> Long") {
      property("section String") = section(decoder)
      property("retract Long") = retraction(decoder)(catching(_.toLong))
    }

  def stringToFloatProperties(decoder: Decoder[String, Float]): Properties =
    new Properties("String -> Float") {
      property("section String") = section(decoder)
      property("retract Float") = retraction(decoder)(catching(_.toFloat))
    }

  def stringToDoubleProperties(decoder: Decoder[String, Double]): Properties =
    new Properties("String -> Double") {
      property("section String") = section(decoder)
      property("retract Double") = retraction(decoder)(catching(_.toDouble))
    }

  def stringToUUIDProperties(decoder: Decoder[String, UUID]): Properties =
    new Properties("String -> UUID") {
      property("section String") = section(decoder)(Arbitrary(Gen.uuid))
      property("retract UUID") = retraction(decoder)(catching(UUID.fromString))
    }

}

class DefaultDecoderTests extends Properties("defaultDecoders") {
  import DefaultChecks._

  include(stringToBooleanProperties(defaultDecoders.decodeStringToBoolean))
  include(stringToByteProperties(defaultDecoders.decodeStringToByte))
  include(stringToShortProperties(defaultDecoders.decodeStringToShort))
  include(stringToIntProperties(defaultDecoders.decodeStringToInt))
  include(stringToLongProperties(defaultDecoders.decodeStringToLong))
  include(stringToFloatProperties(defaultDecoders.decodeStringToFloat))
  include(stringToDoubleProperties(defaultDecoders.decodeStringToDouble))
  include(stringToUUIDProperties(defaultDecoders.decodeStringToUUID))

}

class DefaultReinterpretStringTests extends Properties("Read.Reinterpret") {
  import DefaultChecks._

  def reinterpret[A, B](implicit ev: Read.Reinterpret[A, B]): Decoder[A, B] = ev.decoder

  include(stringToBooleanProperties(reinterpret[String, Boolean]))
  include(stringToByteProperties(reinterpret[String, Byte]))
  include(stringToShortProperties(reinterpret[String, Short]))
  include(stringToIntProperties(reinterpret[String, Int]))
  include(stringToLongProperties(reinterpret[String, Long]))
  include(stringToFloatProperties(reinterpret[String, Float]))
  include(stringToDoubleProperties(reinterpret[String, Double]))
  include(stringToUUIDProperties(reinterpret[String, UUID]))
}
