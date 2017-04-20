package classy_example

import scala.Predef._

import classy._
import classy.config._

object ConfigExample1 extends App {

  case class Entry(
    host: String,
    port: Int)

  case class Services(
    users   : Entry,
    database: Entry)

  // some raw HOCON text
  val confString = """
    foo                    = bar,
    services.database.host = localhost
    services.database.port = 9090
    services.users { host: localhost, port: 9000 }
    """

  // writing a decoder by hand

  val decodeEntry0 = (
    readConfig[String]("host") join
    readConfig[Int]("port")
  ).map(Entry.tupled)

  val decodeServices0 = (
    decodeEntry0.atPath("users") join
    decodeEntry0.atPath("database")
  ).map(Services.tupled).atPath("services")

  // deriving a decoder automatically

  import classy.generic.auto._
  val decodeServices1 = ConfigDecoder[Services].atPath("services")

  def printResult[A](res: Either[DecodeError, A]): Unit =
    res.fold(
      error    => println("Oh no! " + error),
      services => println("Success! " + services))

  val res0 = decodeServices0.fromString(confString)
  val res1 = decodeServices1.fromString(confString)

  printResult(res0)
  printResult(res1)
}
