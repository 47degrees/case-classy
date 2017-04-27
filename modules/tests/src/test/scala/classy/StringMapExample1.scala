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

package classy_example

import scala.Predef._

import classy._
import classy.stringMap._

object StringMapExample1 extends App {

  case class Entry(
    host: String,
    port: Int)

  case class Services(
    users   : Entry,
    database: Entry)

  // some data to load
  // this could come from `sys.env`, if you wanted

  val env: Map[String, String] = Map(
    "foo"                    -> "bar",
    "services.users.host"    -> "localhost",
    "services.users.port"    -> "9000",
    "services.database.host" -> "localhost",
    "services.database.port" -> "9090")

  // writing a decoder by hand

  val decodeEntry0 = (
    readStringMap[String]("host") join
    readStringMap[Int]("port")
  ).map(Entry.tupled)

  val decodeServices0 = (
    decodeEntry0.atPath("users") join
    decodeEntry0.atPath("database")
  ).map(Services.tupled).atPath("services")

  // deriving a decoder automatically

  import classy.generic.auto._
  val decodeServices1 = Decoder[StringMap, Services].atPath("services")

  // running the decoders

  def printResult[A](res: Either[DecodeError, A]): Unit =
    res.fold(
      error    => println("Oh no! " + error),
      services => println("Success! " + services))

  val res0 = decodeServices0(env)
  val res1 = decodeServices1(env)

  printResult(res0)
  printResult(res1)

}
