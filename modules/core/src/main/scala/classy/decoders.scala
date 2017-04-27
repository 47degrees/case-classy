/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package classy

import predef._

import scala.annotation.tailrec
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
      val filtered = filterStringMap(s"$key.", map)
      if (filtered.isEmpty) Missing.atPath(key).left
      else filtered.right
    }

  def stringMapToListString(key: String): Decoder[StringMap, List[String]] =
    decoder { map =>
      @tailrec def next(i: Int, acc: List[String]): List[String] =
        map.get(s"$key[$i]") match {
          case Some(v) => next(i + 1, v :: acc)
          case None    => acc
        }
      val children = next(0, Nil)
      if (children.isEmpty) Missing.atPath(key).left
      else children.right
    }

  def stringMapToListStringMap(key: String): Decoder[StringMap, List[StringMap]] =
    decoder { map =>
      @tailrec def next(i: Int, acc: List[StringMap]): List[StringMap] = {
        val filtered = filterStringMap(s"$key[$i].", map)
        if (filtered.isEmpty) acc
        else next(i + 1, filtered :: acc)
      }
      val children = next(0, Nil)
      if (children.isEmpty) Missing.atPath(key).left
      else children.right
    }

  private[this] def filterStringMap(prefix: String, map: StringMap): StringMap =
    map.toList.collect {
      case (path, value) if path.startsWith(prefix) =>
        (path.substring(prefix.length), value)
    }.toMap

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
