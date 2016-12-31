/* -
 * Case Classy [case-classy-tests]
 */

package classy
package typesafe

import com.typesafe.config._

import scala.Predef._

import org.scalacheck._
import org.scalacheck.Prop._

object TypesafeDecoderProperties {
  case class Key(value: String) extends AnyVal
  implicit val arbitraryKey: Arbitrary[Key] = Arbitrary(
    Gen.alphaStr.map(value ⇒ Key(value)) suchThat (_.value.length > 0))
  implicit def keyToString(key: Key): String = key.value

  implicit def keyValueConfigList(kv: (Key, List[Any])) = {
    val values = kv._2.map(v ⇒ s""""$v"""").mkString(",")
    ConfigFactory parseString s""" ${kv._1.value} = [$values] """
  }

  implicit def keyValueConfig(kv: (Key, Any)) =
    ConfigFactory parseString s""" ${kv._1.value} = "${kv._2.toString}" """
}

class StdTypesafeDecoderProperties extends Properties("TypesafeDecoders.std") {
  import TypesafeDecoderProperties._
  import TypesafeDecoders.std._

  // TODO: just figure out how to escape strings before sticking them into
  // the config (before parsing)
  implicit val arbitraryString: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)

  property("string") = forAll { (key: Key, value: String) ⇒
    string(key).decode(key → value) ?= value.right
  }

  property("stringList") = forAll { (key: Key, value: List[String]) ⇒
    stringList(key).decode(key → value) ?= value.right
  }

  def mashDecimals(x: Number, y: Number): (Number, Number) = (x, y) match {
    case (d: java.lang.Double, f: java.lang.Float) ⇒ (d.floatValue, f)
    case (f: java.lang.Float, d: java.lang.Double) ⇒ (f, d.floatValue)
    case xy                                        ⇒ xy
  }

  property("number") = forAll { (key: Key, value: Number) ⇒
    number(key).decode(key → value).map { parsed ⇒
      val (x, y) = mashDecimals(parsed, value)
      x ?= y
    } getOrElse falsified
  }

  property("numberList") = forAll { (key: Key, value: List[Number]) ⇒
    numberList(key).decode(key → value).map { parsed ⇒
      val (x, y) = (parsed zip value).map(xy ⇒ mashDecimals(xy._1, xy._2)).unzip
      x ?= y
    } getOrElse falsified
  }

  property("boolean") = forAll { (key: Key, value: Boolean) ⇒
    boolean(key).decode(key → value) ?= value.right
  }

  property("booleanList") = forAll { (key: Key, value: List[Boolean]) ⇒
    booleanList(key).decode(key → value) ?= value.right
  }

  property("int") = forAll { (key: Key, value: Int) ⇒
    int(key).decode(key → value) ?= value.right
  }

  property("intList") = forAll { (key: Key, value: List[Int]) ⇒
    intList(key).decode(key → value) ?= value.right
  }

  property("long") = forAll { (key: Key, value: Long) ⇒
    long(key).decode(key → value) ?= value.right
  }

  property("longList") = forAll { (key: Key, value: List[Long]) ⇒
    longList(key).decode(key → value) ?= value.right
  }

  property("double") = forAll { (key: Key, value: Double) ⇒
    double(key).decode(key → value) ?= value.right
  }

  property("doubleList") = forAll { (key: Key, value: List[Double]) ⇒
    doubleList(key).decode(key → value) ?= value.right
  }

}
