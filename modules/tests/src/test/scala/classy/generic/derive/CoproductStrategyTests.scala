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
package generic
package derive

import predef._
import scala.Predef.ArrowAssoc

import org.scalacheck._
import org.scalacheck.Prop._

import scala.collection.immutable.Map
import scala.reflect.ClassTag

class CoproductStrategyTests extends Properties("CoproductStrategy") {
  import DecodeError._

  implicit def readMap[A](implicit evA: ClassTag[A]): Read[Map[String, Any], A] = Read.instance(
    path => Decoder.instance(_.get(path) match {
      case Some(evA(a)) => a.right
      case _            => AtPath(path, Missing).left
    }))

  case class Entry[A, B](
    input: A,
    decoder: Decoder[A, B],
    name: String,
    output: Either[DecodeError, B]
  )

  val nestedEntries: List[Entry[Map[String, Any], String]] = List(
    Entry(
      Map("foo" -> Map("foo" -> "bar")),
      readMap[String].apply("foo"),
      "Foo",
      "bar".right
    ),
    Entry(
      Map.empty,
      readMap[String].apply("foo"),
      "Foo",
      AtPath("foo", Missing).left
    ),
    Entry(
      Map("foo" -> Map.empty),
      readMap[String].apply("foo"),
      "Foo",
      AtPath("foo", AtPath("foo", Missing)).left
    )
  )

  val nested = CoproductStrategy.Nested()

  property("Nested") =
    nestedEntries
      .map(entry => nested.decoder(entry.decoder, entry.name).apply(entry.input) ?= entry.output)
      .reduce(_ && _)

  val typedEntries: List[Entry[Map[String, Any], String]] = List(
    Entry(
      Map("type" -> "foo", "foo" -> "bar"),
      readMap[String].apply("foo"),
      "Foo",
      "bar".right
    ),
    Entry(
      Map("type" -> "foo"),
      readMap[String].apply("foo"),
      "Foo",
      AtPath("foo", Missing).left
    )
  )

  val typed = CoproductStrategy.Typed()

  property("Typed") =
    typedEntries
      .map(entry => typed.decoder(entry.decoder, entry.name).apply(entry.input) ?= entry.output)
      .reduce(_ && _)

}
