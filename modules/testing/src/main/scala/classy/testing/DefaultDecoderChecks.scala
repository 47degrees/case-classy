/* -
 * Case Classy [classy-testing]
 */

package classy
package testing

import predef._
import scala.Predef.{ augmentString, ArrowAssoc, classOf }

import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._

import shapeless.Typeable

import java.util.UUID

object DefaultDecoderChecks {

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

  def properties(name: String)(props: (String, Prop)*): Properties = {
    val properties = new Properties(name)
    props.foreach(p => properties.property(p._1) = p._2)
    properties
  }

  def stringToBooleanProperties(decoder: Decoder[String, Boolean]): Properties =
    properties("String to Boolean")(
      "section String"  -> section(decoder),
      "retract Boolean" -> retraction(decoder)(catching(_.toBoolean)))

  def stringToByteProperties(decoder: Decoder[String, Byte]): Properties =
    properties("String to Byte")(
      "section String"  -> section(decoder),
      "retract Byte"    -> retraction(decoder)(catching(_.toByte)))

  def stringToShortProperties(decoder: Decoder[String, Short]): Properties =
    properties("String to Short")(
      "section String"  -> section(decoder),
      "retract Short"   -> retraction(decoder)(catching(_.toShort)))

  def stringToIntProperties(decoder: Decoder[String, Int]): Properties =
    properties("String to Int")(
      "section String"  -> section(decoder),
      "retract Int"     -> retraction(decoder)(catching(_.toInt)))

  def stringToLongProperties(decoder: Decoder[String, Long]): Properties =
    properties("String to Long")(
      "section String"  -> section(decoder),
      "retract Long"    -> retraction(decoder)(catching(_.toLong)))

  def stringToFloatProperties(decoder: Decoder[String, Float]): Properties =
    properties("String to Float")(
      "section String"  -> section(decoder),
      "retract Float"   -> retraction(decoder)(catching(_.toFloat)))

  def stringToDoubleProperties(decoder: Decoder[String, Double]): Properties =
    properties("String to Double")(
      "section String"  -> section(decoder),
      "retract Double"  -> retraction(decoder)(catching(_.toDouble)))

  def stringToUUIDProperties(decoder: Decoder[String, UUID]): Properties =
    properties("String to UUID")(
      "section String"  -> section(decoder)(Arbitrary(Gen.uuid)),
      "retract UUID"    -> retraction(decoder)(catching(UUID.fromString)))

}
