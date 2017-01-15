/* -
 * Case Classy [case-classy-tests]
 */

package classy_generic_examples

import com.typesafe.config.ConfigFactory
import scala.Predef._

object Example1 extends App {

  case class Bar(value: String)

  case class Foo(
    a: String = "a's default value",
    b: Option[Int],
    c: List[String],
    bars: List[Bar]
  )

  val typesafeConfig = ConfigFactory parseString """
   | a : okay
   | c = ["hello", "world"]
   | bars = [{ value: hello }]
   |""".stripMargin

  {
    // Automatically derive decoders
    import classy.config._
    import classy.generic.auto._

    val res = ConfigDecoder[Foo].decode(typesafeConfig)
    println("Zed: " + res)
  }

  {
    // Assemble a decoder with some help
    import classy.config._
    import classy.generic.deriveDecoder
    import com.typesafe.config.Config

    implicit val decodeBar = deriveDecoder[Config, Bar]
    implicit val decodeFoo = deriveDecoder[Config, Foo]

    val res = ConfigDecoder[Foo].apply(typesafeConfig)
    println("Yax: " + res)
  }

  {
    // Assemble a decoder by hand
    import com.typesafe.config.Config
    import classy.config._

    val decodeA    = readConfig[String]("a").withDefault("a's default value")
    val decodeB    = readConfig[Int]("b").optional
    val decodeC    = readConfig[List[String]]("c")
    val decodeBar  = readConfig[String]("value").map(value => Bar(value))
    val decodeBars = readConfig[List[Config]]("bars") andThen decodeBar.sequence[List]

    implicit val decodeFoo = (decodeA and decodeB and decodeC and decodeBars).map {
      case (((a, b), c), bar) => Foo(a, b, c, bar)
    }

    val res = ConfigDecoder[Foo].apply(typesafeConfig)
    println("Xan: " + res)
  }



  {
    // Configuring your generic decoders

    import com.typesafe.config.Config
    import classy.config._
    import classy.generic._

    val res = makeDecoder[Config, Foo]
      .withOptions
      .renameFields(_.toUpperCase)
      .decoder
      .fromString
      .apply("""
        A = OKAY
        B = 1235
        C = [ZZZ]
        BARS = [{ VALUE: hi }]
        """)

    println("Zoof: " + res)
  }


}
