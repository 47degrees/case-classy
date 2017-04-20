/* -
 * Case Classy [classy-tests]
 */

package classy_example

import scala.Predef._

import classy.stringMap._
import classy.generic.auto._

object PrettyErrorsExample1 extends App {


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
