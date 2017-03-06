/* -
 * Case Classy [classy-core]
 */

package classy

import predef._

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import java.io.File            //#=jvm
import java.io.FileInputStream //#=jvm
import java.io.InputStream     //#=jvm
import java.util.UUID
import java.util.Properties    //#=jvm

/** Default decoders that can be used as building blocks
  * to create new decoders
  */
package object decoders {

  import Decoder.{ instance => decoder }
  import DecodeError._

  // Catches exceptions and returns them on the left side of an Either
  private[this] def guard[T](f: => T): Either[Throwable, T] =
    try f.right
    catch {
      case t: Throwable => t.left
    }

  val stringToBoolean: Decoder[String, Boolean] =
    decoder(value => value.toLowerCase match {
      case "true"  => true.right
      case "false" => false.right
      case _       => WrongType("Boolean", value.some).left
    })

  val stringToByte: Decoder[String, Byte] =
    decoder(value => guard(java.lang.Byte.parseByte(value))
      .leftMap(_ => WrongType("Byte", value.some)))

  val stringToShort: Decoder[String, Short] =
    decoder(value => guard(java.lang.Short.parseShort(value))
      .leftMap(_ => WrongType("Short", value.some)))

  val stringToInt: Decoder[String, Int] =
    decoder(value => guard(java.lang.Integer.parseInt(value))
      .leftMap(_ => WrongType("Int", value.some)))

  val stringToLong: Decoder[String, Long] =
    decoder(value => guard(java.lang.Long.parseLong(value))
      .leftMap(_ => WrongType("Long", value.some)))

  val stringToFloat: Decoder[String, Float] =
    decoder(value => guard(java.lang.Float.parseFloat(value))
      .leftMap(_ => WrongType("Float", value.some)))

  val stringToDouble: Decoder[String, Double] =
    decoder(value => guard(java.lang.Double.parseDouble(value))
      .leftMap(_ => WrongType("Double", value.some)))

  val stringToUUID: Decoder[String, UUID] =
    decoder(value => guard(UUID.fromString(value))
      .leftMap(_ => WrongType("UUID", value.some)))

  val stringToDuration: Decoder[String, Duration] =
    decoder(value => guard(Duration(value))
      .leftMap(_ => WrongType("Duration", value.some)))

  val durationToFiniteDuration: Decoder[Duration, FiniteDuration] =
    decoder(_ match {
      case finiteDuration: FiniteDuration => finiteDuration.right
      case duration                       =>
        WrongType("FiniteDuration", s"Duration($duration)".some).left
    })

  val stringToFiniteDuration: Decoder[String, FiniteDuration] =
    stringToDuration >>> durationToFiniteDuration

  type StringMap = scala.collection.Map[String, String]

  def stringMapToString(key: String): Decoder[StringMap, String] =
    decoder(map =>
      map.get(key).toRight(Missing.atPath(key)))

  def stringMapToStringMap(key: String): Decoder[StringMap, StringMap] =
    decoder { map =>
      val prefix = s"$key."
      val filtered = map.toList
        .collect {
        case (path, value) if path.startsWith(prefix) =>
          (path.substring(prefix.length), value)
      }.toMap

      if (filtered.isEmpty)
        Missing.atPath(key).left
      else
        filtered.right
    }

  //#+jvm
  val inputStreamToProperties: Decoder[InputStream, Properties] =
    Decoder.instance { inputStream =>
      val properties = new Properties()
      try {
        properties.load(inputStream)
        properties.right
      } catch {
        case e: Throwable => Underlying(e).left
      }
    }
  //#-jvm

  //#+jvm
  val fileToInputStream: Decoder[File, InputStream] =
    Decoder.instance { file =>
      try {
        new FileInputStream(file).right
      } catch {
        case e: Throwable => Underlying(e).left
      }
    }
  //#-jvm

}
