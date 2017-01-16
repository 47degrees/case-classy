/* -
 * Case Classy [classy-tests]
 */

package classy
package generic
package derive

import org.scalacheck._
import org.scalacheck.Prop._

import scala.Predef._

class NamingStrategyTests extends Properties("NamingStrategyTests") {
  import NamingStrategy._

  case class Entry(input: String, output: String)

  private[this] def check(namer: NamingStrategy, entries: List[Entry]): Prop =
    entries
      .map(entry => (namer.name(entry.input) ?= entry.output) :| s"checking $entry")
      .reduce(_ && _)

  property("Identity") = forAll((input: String) =>
    Identity.name(input) ?= input)

  property("Basic") = forAll((input: String, run: String => String) =>
    Basic(run).name(input) ?= run(input))

  property("CamelCase") = check(
    CamelCase,
    List(
      Entry("hello", "hello"),
      Entry("HelloWorld", "helloWorld"),
      Entry("helloWorld", "helloWorld"),
      Entry("Helloworld", "helloworld")
    ))

  property("SnakeCase") = check(
    SnakeCase,
    List(
      Entry("hello", "hello"),
      Entry("HelloWorld", "Hello_World"),
      Entry("helloWorld", "hello_World"),
      Entry("Helloworld", "Helloworld")
    ))

  property("UpperSnakeCase") = check(
    UpperSnakeCase,
    List(
      Entry("hello", "HELLO"),
      Entry("HelloWorld", "HELLO_WORLD"),
      Entry("helloWorld", "HELLO_WORLD"),
      Entry("Helloworld", "HELLOWORLD")
    ))

  property("LowerSnakeCase") = check(
    LowerSnakeCase,
    List(
      Entry("hello", "hello"),
      Entry("HelloWorld", "hello_world"),
      Entry("helloWorld", "hello_world"),
      Entry("Helloworld", "helloworld")
    ))

}
