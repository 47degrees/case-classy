/* -
 * Case Classy [case-classy-tests]
 */

package classy
package generic_

import com.typesafe.config.ConfigFactory
import scala.Predef._

object Example1 extends App {

  case class Bar(value: String)

  case class Foo(
    a: String,
    b: Option[Int],
    c: List[String],
    bars: List[Bar]
  )

  val typesafeConfig = ConfigFactory parseString """
   | a = 1
   | c = ["hello", "world"]
   | bars = [{ value: hello }]
   |""".stripMargin

  {
    // Level Zed: Full Black Magic
    import classy.typesafe._
    import classy.generic.auto._

    val res = TypesafeDecoder[Foo].decode(typesafeConfig)
    println("Zed: " + res)
  }

  {
    // Level Yax: Partial Black Magic
    import classy.typesafe._
    import classy.generic.deriveDecoder
    import com.typesafe.config.Config

    implicit val decodeBar = deriveDecoder[Config, Bar]
    implicit val decodeFoo = deriveDecoder[Config, Foo]

    val res = TypesafeDecoder[Foo].decode(typesafeConfig)
    println("Yax: " + res)
  }

  {
    // Level Xan: Has No Magic
    import classy.typesafe._
    import TypesafeDecoders.std._

    val decodeA = string("a")
    val decodeB = int("b").option
    val decodeC = stringList("c")
    val decodeBar = string("value").map(value ⇒ Bar(value))
    val decodeBars = configList("bars") andThen decodeBar.sequence

    implicit val decodeFoo = (decodeA and decodeB and decodeC and decodeBars).map {
      case (((a, b), c), bar) ⇒ Foo(a, b, c, bar)
    }

    val res = TypesafeDecoder[Foo].decode(typesafeConfig)
    println("Xan: " + res)
  }

}
