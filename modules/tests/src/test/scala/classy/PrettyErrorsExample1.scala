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

import classy.stringMap._
import classy.generic.auto._

object PrettyErrorsExample1Data {
  case class A(a: B)
  case class B(b: C)
  case class C(c: D)
  case class D(d: String)

  sealed trait FooBar
  case class Foo(value: String) extends FooBar
  case class Bar(value: String) extends FooBar

  case class Config(
    z0: A,
    z1: B,
    z2: C,
    z3: D,
    fooBar: FooBar,
    maybeFooBar: Option[FooBar],
    listFooBar: List[FooBar],
    listDouble: List[String])
}

object PrettyErrorsExample1 extends App {
  import PrettyErrorsExample1Data._

  val decoder = StringMapDecoder[Config]

  val inputs: List[StringMap] = List(
    Map.empty,
    Map("z0" -> ""),
    Map("z0.a" -> ""),
    Map("z0.a.b" -> ""),
    Map("z0.a.b.c" -> ""),
    Map(
      "z0.a.b.c" -> "",
      "z1.a.b.c" -> ""),
    Map(
      "fooBar.foo" -> "asdf"),
    Map(
      "listFooBar[0].foo" -> "",
      "listFooBar[1].foo" -> ""))

  inputs.foreach { input =>
    println("input:")
    println(input)
    println("result:")
    decoder(input) match {
      case Left(err) =>
        println(err)
        println(err.toPrettyString)
      case Right(_)  =>
    }
    println("")
  }


}
