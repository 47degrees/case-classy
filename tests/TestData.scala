/* -
 * Case Classy [tests]
 */

package classytests

object TestData {

  case class FooBar(
    foo: String,
    bar: Int
  )

  case class TwoFooBars(
    fb1: FooBar,
    fb2: FooBar
  )

  val validFooBar = FooBar("foo", 1)
  val validTwoFooBars =
    TwoFooBars(validFooBar, validFooBar)

}
