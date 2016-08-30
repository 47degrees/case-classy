/* -
 * Case Classy [tests]
 */

package classytests

import classy._
import classy.typesafe._

import com.typesafe.config.ConfigFactory

import org.specs2.mutable.Specification
import org.specs2.matcher._

class ReallySimpleTypesafeSpec extends Specification
    with ResultMatchers with XorMatchers {

  import TestData._

  implicit val decodeFooBar: TypesafeDecoder[FooBar] = deriveTypesafeDecoder[FooBar]
  implicit val decodeTwoFooBars: TypesafeDecoder[TwoFooBars] =
    deriveTypesafeDecoder[TwoFooBars]

  "Typesafe Config decoding" >> {

    val validFooBarConfig = ConfigFactory parseString """
      |foo: "foo"
      |bar:  1
      |""".stripMargin

    val validTwoFooBarsConfig = ConfigFactory parseString """
      |fb1.foo = "foo"
      |fb1.bar = 1
      |fb2 = { foo: "foo", bar = 1}
      |""".stripMargin

    "handles basic data types" >> {
      TypesafeDecoder[FooBar].apply(validFooBarConfig) must
        beXorRight(validFooBar)
    }

    "handles nested data types" >> {
      TypesafeDecoder[TwoFooBars].apply(validTwoFooBarsConfig) must
        beXorRight(validTwoFooBars)

    }
  }

}
