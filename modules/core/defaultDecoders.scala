/* -
 * Case Classy [classy-core]
 */

package classy
package core

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import java.util.UUID

import Decoder.{ instance => decoder }
import DecodeError.WrongType

/** Default decoders for various common types
  *
  * Most of these decoders are from `String` to another type.
  */
package object defaultDecoders {

  // Catches exceptions and returns them on the left side of an Either
  private[this] def guard[T](f: => T): Either[Throwable, T] =
    try f.right
    catch {
      case t: Throwable => t.left
    }

  // Decoding of primitive types. This is done in the same manner as
  // the `str.toBoolean`, `str.toByte` etc ops available with
  // scala.Predef.

  val decodeStringToBoolean: Decoder[String, Boolean] =
    decoder(value => value.toLowerCase match {
      case "true"  => true.right
      case "false" => false.right
      case _       => WrongType("Boolean", value.some).left
    })

  val decodeStringToByte: Decoder[String, Byte] =
    decoder(value => guard(java.lang.Byte.parseByte(value))
      .leftMap(_ => WrongType("Byte", value.some)))

  val decodeStringToShort: Decoder[String, Short] =
    decoder(value => guard(java.lang.Short.parseShort(value))
      .leftMap(_ => WrongType("Short", value.some)))

  val decodeStringToInt: Decoder[String, Int] =
    decoder(value => guard(java.lang.Integer.parseInt(value))
      .leftMap(_ => WrongType("Int", value.some)))

  val decodeStringToLong: Decoder[String, Long] =
    decoder(value => guard(java.lang.Long.parseLong(value))
      .leftMap(_ => WrongType("Long", value.some)))

  val decodeStringToFloat: Decoder[String, Float] =
    decoder(value => guard(java.lang.Float.parseFloat(value))
      .leftMap(_ => WrongType("Float", value.some)))

  val decodeStringToDouble: Decoder[String, Double] =
    decoder(value => guard(java.lang.Double.parseDouble(value))
      .leftMap(_ => WrongType("Double", value.some)))

  // Decoding of other basic types

  val decodeStringToUUID: Decoder[String, UUID] =
    decoder(value => guard(UUID.fromString(value))
      .leftMap(_ => WrongType("UUID", value.some)))

  val decodeStringToDuration: Decoder[String, Duration] =
    decoder(value => guard(Duration(value))
      .leftMap(_ => WrongType("Duration", value.some)))

  val decodeDurationToFiniteDuration: Decoder[Duration, FiniteDuration] =
    decoder(_ match {
      case finiteDuration: FiniteDuration => finiteDuration.right
      case duration                       =>
        WrongType("FiniteDuration", s"Duration($duration)".some).left
    })

  val decodeStringToFiniteDuration: Decoder[String, FiniteDuration] =
    decodeDurationToFiniteDuration compose decodeStringToDuration

}
