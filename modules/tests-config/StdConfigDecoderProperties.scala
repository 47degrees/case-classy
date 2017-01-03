/* -
 * Case Classy [case-classy-tests]
 */

package classy
package config

import com.typesafe.config._

import scala.Predef._

import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary

import core._
import testing._

object StdConfigDecoderProperties {
  case class Key(value: String) extends AnyVal
  implicit val arbitraryKey: Arbitrary[Key] = Arbitrary(
    Gen.alphaStr.map(value => Key(value + "_")))
  implicit def keyToString(key: Key): String = key.value

  implicit def keyValueConfigList(kv: (Key, List[Any])) = {
    val values = kv._2.map(v => s""""$v"""").mkString(",")
    ConfigFactory parseString s""" ${kv._1.value} = [$values] """
  }

  implicit def keyValueConfig(kv: (Key, Any)) =
    ConfigFactory parseString s""" ${kv._1.value} = "${kv._2.toString}" """
}

class StdConfigDecoderProperties extends Properties(s"${ConfigDecoders.BACKEND} ConfigDecoder.std") {
  import StdConfigDecoderProperties._

  // TODO: just figure out how to escape strings before sticking them into
  // the config (before parsing)
  implicit val arbitraryString: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)
  implicit val arbConfig: Arbitrary[Config] = Arbitrary(
    for {
      key <- arbitrary[Key]
      value <- arbitrary[String]
    } yield keyValueConfig(key -> value)
  )


  def reading[A](mash: (A, A) => A)(implicit
    read: Read[Config, A],
    toConfig: ((Key, A)) => Config
  ): (A) => (Config, ConfigDecoder[A]) = { result =>
    val key = arbitrary[Key].sample.get
    (toConfig(key -> result), read(key).map(a => mash(a, result)))
  }

  def reading[A](implicit
    read: core.Read[Config, A],
    toConfig: ((Key, A)) => Config
  ): (A) => (Config, ConfigDecoder[A]) = reading[A]((a: A, r: A) => a)


  include(DecoderChecks.positive(reading[String]), "String ")
  include(DecoderChecks.positive(reading[List[String]]), "List[String] ")
  include(DecoderChecks.positive(reading[Boolean]), "Boolean ")
  include(DecoderChecks.positive(reading[List[Boolean]]), "List[Boolean] ")
  include(DecoderChecks.positive(reading[Int]), "Int ")
  include(DecoderChecks.positive(reading[List[Int]]), "List[Int] ")
  include(DecoderChecks.positive(reading[Long]), "Long ")
  include(DecoderChecks.positive(reading[List[Long]]), "List[Long] ")
  include(DecoderChecks.positive(reading[Double]), "Double ")
  include(DecoderChecks.positive(reading[List[Double]]), "List[Double] ")



  //#+typesafe

  implicit val cogenNumber = Cogen((_: Number).hashCode.toLong)


  // For generated numbers that happen to be floats, it's possible
  // that the parsed value comes back as a double. Similarly, for
  // generated values that are doubles it's possible that they come
  // back parsed as floats. These helpers mash the actual return from
  // the decoders to the type of the seed return

  val mash: (Number, Number) => Number = (_, _) match {
    case (a: java.lang.Double, r: java.lang.Float) => a.floatValue
    case (a: java.lang.Float, r: java.lang.Double) => a.doubleValue
    case (a, _) => a
  }

  val listMash: (List[Number], List[Number]) => List[Number] = { (a, r) =>
    (a zip r).map(xy => mash(xy._1, xy._2))
  }

  include(DecoderChecks.positive(reading[Number](mash)), "Number ")
  include(DecoderChecks.positive(reading[List[Number]](listMash)), "List[Number] ")

  //#-typesafe


}
