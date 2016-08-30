/* -
 * Case Classy [tests]
 */

package classytests

import classy.map._

import org.specs2.mutable.Specification
import org.specs2.matcher._

class ReallySimpleMapSpec extends Specification
    with ResultMatchers with XorMatchers {

  private[this] val validFooBarData = Map(
    "string" → "foo",
    "int" → 1)

  private[this] val validFooBar = FooBar("foo", 1)

  private[this] val validTwoFooBarsData = Map(
    "fb1" → validFooBarData,
    "fb2" → validFooBarData)

  private[this] val validTwoFooBars =
    TwoFooBars(validFooBar, validFooBar)

  "Map decoding" >> {

    "handles basic data types" >> {
      MapDecoder[FooBar].apply(validFooBarData) must
        beXorRight(validFooBar)
    }

    "handles nested data types" >> {
      MapDecoder[TwoFooBars].apply(validTwoFooBarsData) must
        beXorRight(validTwoFooBars)
    }
  }

}
