/* -
 * Case Classy [tests]
 */

package classytests

import classy.map._

import org.specs2.mutable.Specification
import org.specs2.matcher._

class ReallySimpleMapSpec extends Specification
    with ResultMatchers with XorMatchers {

  import TestData._

  implicit val decodeFooBar: MapDecoder[FooBar] = deriveMapDecoder[FooBar]
  implicit val decodeTwoFooBars: MapDecoder[TwoFooBars] =
    deriveMapDecoder[TwoFooBars]

  "Map decoding" >> {

    val validFooBarMap = Map(
      "foo" → "foo",
      "bar" → 1)

    val validTwoFooBarsMap = Map(
      "fb1" → validFooBarMap,
      "fb2" → validFooBarMap)

    "handles basic data types" >> {
      MapDecoder[FooBar].apply(validFooBarMap) must
        beXorRight(validFooBar)
    }

    "handles nested data types" >> {
      MapDecoder[TwoFooBars].apply(validTwoFooBarsMap) must
        beXorRight(validTwoFooBars)
    }
  }

}
