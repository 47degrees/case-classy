package classy
package generic

import org.scalacheck._
import org.scalacheck.Prop._

import core.DecodeError._
import config._
import auto._

import scala.Predef.augmentString

object GenericConfigDecoderErrorProperties {

  object FooADT {
    case class Foo(foo: Bar)
    case class Bar(bar: Baz)
    case class Baz(baz: Zip)
    case class Zip(zip: String)
  }

  object ShapeADT {
    sealed abstract class Shape(fa: => Double) extends Product with Serializable { self =>
      lazy val area: Double = fa
    }
    object Shape {
      final case class Circle(radius: Double) extends Shape(
        math.Pi * radius * radius)
      final case class Square(dimension: Double) extends Shape(
        dimension * dimension)
      final case class Rectangle(length: Double, width: Double) extends Shape(
        length * width)
      final case class Triangle(base: Double, height: Double) extends Shape(
        0.5 * base * height)
      final case class RegularPolygon(dimension: Double, sides: Long) extends Shape({
        val s = dimension
        val n = sides.toDouble
        s*s*n / (4 * math.tan(180/n))
      })
    }

    case class Shapes(shapes: List[Shape])
  }

}

class GenericConfigDecoderErrorProperties extends Properties("generic ConfigDecoder errors") {
  import GenericConfigDecoderErrorProperties._

  import FooADT._
  val fooDecoder = ConfigDecoder[Foo].fromString

  property("decode foo error 1") =
    fooDecoder.decode("wrong { }") ?=
      MissingPath("foo").left

  property("decode foo error 1") =
    fooDecoder.decode("foo { }") ?=
      AtPath("foo",
        MissingPath("bar")).left

  property("decode foo error 1") =
    fooDecoder.decode("foo { bar {} }") ?=
      AtPath("foo",
        AtPath("bar",
           MissingPath("baz"))).left

  property("decode foo error 1") =
    fooDecoder.decode("foo { bar { baz {} } }") ?=
      AtPath("foo",
        AtPath("bar",
          AtPath("baz",
            MissingPath("zip")))).left

  import ShapeADT._
  val shapeDecoder = ConfigDecoder[Shape].fromString
  val shapesDecoder = ConfigDecoder[Shapes].fromString

  property("decode shape errors") =
    shapeDecoder.decode("wrong {}") ?=
      Or(
        MissingPath("circle"),
        MissingPath("rectangle"),
        MissingPath("regularPolygon"),
        MissingPath("square"),
        MissingPath("triangle")).left

  property("decode shape errors with nested error") =
    shapeDecoder.decode("circle { stillWrong: {} }") ?=
      Or(
        AtPath("circle",
          MissingPath("radius")),
        MissingPath("rectangle"),
        MissingPath("regularPolygon"),
        MissingPath("square"),
        MissingPath("triangle")).left

  property("decode shapes error") =
    shapesDecoder.decode("wrong {}") ?=
      MissingPath("shapes").left

  property("decode shapes errors with nested errors 1") =
    shapesDecoder.decode("shapes: [{}, {}]") ?=
      AtPath("shapes", And(
        AtIndex(0, Or(
          MissingPath("circle"),
          MissingPath("rectangle"),
          MissingPath("regularPolygon"),
          MissingPath("square"),
          MissingPath("triangle"))),
        AtIndex(1, Or(
          MissingPath("circle"),
          MissingPath("rectangle"),
          MissingPath("regularPolygon"),
          MissingPath("square"),
          MissingPath("triangle"))))).left

  property("decode shapes errors with nested errors 2") =
    shapesDecoder.decode("""
      |shapes: [
      |  { circle { radius: 2 } },
      |  { rectangle {} },
      |  { square { dimension: 2 } },
      |  { square {} }
      |]""".stripMargin.replace("\n", "")
    ) ?=
      AtPath("shapes",
        And(
          AtIndex(1, Or(
            MissingPath("circle"),
            AtPath("rectangle", And(
              MissingPath("length"),
              MissingPath("width"))),
            MissingPath("regularPolygon"),
            MissingPath("square"),
            MissingPath("triangle"))),
          AtIndex(3, Or(
            MissingPath("circle"),
            MissingPath("rectangle"),
            MissingPath("regularPolygon"),
            AtPath("square",
              MissingPath("dimension")),
            MissingPath("triangle")))
        )).left

}
