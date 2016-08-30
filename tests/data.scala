/* -
 * Case Classy [tests]
 */

package classytests

import classy.map._

case class FooBar(
  string: String,
  int: Int
)

object FooBar {
  implicit val decodeMap: MapDecoder[FooBar] = deriveMapDecoder[FooBar]
}

case class TwoFooBars(
  fb1: FooBar,
  fb2: FooBar
)

object TwoFooBars {
  implicit val decodeMap: MapDecoder[TwoFooBars] =
    deriveMapDecoder[TwoFooBars]
}
