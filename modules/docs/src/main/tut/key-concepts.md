---
layout: page
title:  "Concepts"
section: "Concepts"
position: 2
---

```tut:invisible
import classy.core.DecodeError
```

# <a name="key-concepts" class="anchor" href="#key-concepts">Key Concepts</a>

The main Classy type is [`Decoder`][Decoder], which
captures the ability to decode data of type `A` to type `B`. It has a
single unimplemented method [`apply`][Decoder.apply] that either returns an error or
the result.

```tut:silent
trait Decoder[A, B] {
  def apply(a: A): Either[DecodeError, B]
}
```

[Decoder][Decoder] has many other final methods to facilitate combining and
chaining decoders. For example, we can create a dummy decoder and
then map the result:

```tut:reset:invisible
import classy.core._
```

```tut:silent
// create a decoder for Int that always succeeds with the value
val decoder0: Decoder[Int, Int] = Decoder.instance(value => Right(value))

// create a new decoder by mapping the result to a string
val decoder1: Decoder[Int, String] = decoder0.map(_.toString)
```

And we can easily [decode to `Option`][Decoder.optional]:

```tut:silent
val decoder2: Decoder[Int, Option[String]] = decoder1.optional
```

Or decode a [lists of inputs to a list of outputs][Decoder.sequence]:

```tut:silent
val decoder3: Decoder[List[Int], List[String]] = decoder1.sequence[List]
```

Decoding primitive types, such as Int, isn't tremendously useful on it's own.
What we really want to do is decode more complicated data structures, such as
a `Map` or a `Config` object.

```tut:silent
def decodeString(path: String): Decoder[Map[String, String], String] =
  Decoder.instance(_.get(path).toRight(DecodeError.Missing.atPath(path)))

val decodeA = decodeString("a")
val decodeB = decodeString("b")

val decodeAandB: Decoder[Map[String, String], (String, String)] =
  decodeA join decodeB

case class Res(a: String, b: String)

val decodeRes: Decoder[Map[String, String], Res] =
  decodeAandB map Res.tupled
```

Now we can decode some values and see what happens:

```tut
decodeRes(Map("hello" -> "world"))
decodeRes(Map("a" -> "foo"))
decodeRes(Map("a" -> "foo", "b" -> "bar"))
```

Above we created a helper method `decodeString` that let us easily create
a decoder for a path within our `Map`. This pattern is very common. In fact,
it's so ubiquitous that we introduce a type to capture this pattern:

```tut:silent:fail
class Read[A, B] {
  def apply(path: String): Decoder[A, B]
}
```

It's unlikely that you'll need to
use [`Read`][Read] directly since Case Classy
provides most helpers and decoders in the standard modules.  However,
it's an important foundation for the library and occasionally shows up
in implicit errors if you're decoding types that aren't already
supported.

## <a name="derived-decoders" class="anchor" href="#derived-decoders">Deriving decoders automatically</a>

Deriving decoders automatially is straightforward.
The [`classy-generic` module][modules] uses
[shapeless](https://github.com/milessabin/shapeless) to
automatically create decoders for your product and coproduct types.
Generally this means you can quickly create decoders for almost any
case class hierarchy.

Consider a simple case class hierarchy with a series of `Shape` types
that we'd like to load from configuration:

```tut:silent
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(length: Double, width: Double) extends Shape

case class MyConfig(
  someString: Option[String],
  shapes: List[Shape])
```

We'll derive decoders for Typesafe/Shocon `Config` objects, so we need
to include the [`classy-config` module][modules] in addition to
[`classy-generic`][modules].

```tut:silent
import classy.core.DecodeError
import classy.generic._

import classy.config._
import com.typesafe.config.{ Config, ConfigFactory }
```

We'll use the [`deriveDecoder`][deriveDecoder] method that's part
of [`classy.generic`][classy.generic] to automatically summon a
decoder for our case class hierarchy. We could also use
the [`makeDecoder`][makeDecoder] method if we wanted to tweak some of
the options for our derived decoder.

```tut:silent
val decoder1: ConfigDecoder[MyConfig] = deriveDecoder[Config, MyConfig]
```

*Note: [`ConfigDecoder[A]`][ConfigDecoder] is a type alias for [`Decoder[Config, A]`][Decoder]*

That's it! Now we can decode our configs.

```tut:book
decoder1(ConfigFactory.load()) // shortcut: decoder1.load()

decoder1(ConfigFactory.parseString("""
  |someString = "hello"
  |shapes = [
  | { circle    : { radius: 5 } },
  | { rectangle : { length: 10, width: 10 } }
  |]
  """.stripMargin)) // shortcut: Decoder1.fromString(...)
```

### <a name="coproduct-options" class="anchor" href="#coproduct-options">Coproduct Options</a>

The default behavior for coproducts (sealed trait hierarchies) is to
simply try nested paths using a "camelCase" variation of the subtype's name.
We can use [`makeDecoder`][makeDecoder] to configure this differently.

```tut:silent
val decoder2: ConfigDecoder[MyConfig] = makeDecoder[Config, MyConfig].
  withOptions.
  typeCoproducts(). // default is nestCoproducts
  decoder
```
```tut:book
decoder2(ConfigFactory.parseString("""
  |someString = "hello"
  |shapes = [
  | { type: circle, radius: 5 },
  | { type: rectangle, length: 10, width: 10 }
  |]
  """.stripMargin)) // shortcut: Decoder1.fromString(...)
```

The input configuration to `decoder2` is structured differently than
the input to `decoder1`: each shape subtype is now specified by the
`type` field. The name of this field can also be configured as a
parameter to [`typeCoproducts`][MkDecoderWithOptions.typeCoproducts].

<table style="width:100%">
  <tr>
    <th>Nested (default)</th>
    <th>Typed</th>
  </tr>
  <tr>
    <td>
<pre>shapes = [
  { circle    : { radius: 5 } },
  { rectangle : { length: 10, width: 10 } }
]</pre>
    </td>
    <td>
<pre>shapes = [
  { type: circle, radius: 5 },
  { type: rectangle, length: 10, width: 10 }
]</pre>
    </td>
  </tr>
</table>

[Decoder]: api/classy/core/Decoder.html
[Decoder.apply]: api/classy/core/Decoder.html#apply(a:A):scala.util.Either[classy.core.DecodeError,B]
[Decoder.optional]: api/classy/core/Decoder.html#optional:classy.core.Decoder[A,Option[B]]
[Decoder.sequence]: api/classy/core/Decoder.html#sequence[F[_]](implicitFt:classy.core.wheel.Traversable[F],implicitFi:classy.core.wheel.Indexed[F]):classy.core.Decoder[F[A],F[B]]
[Read]: api/classy/core/Read.html
[modules]: index.html#modules
[classy.generic]: api/classy/generic/
[deriveDecoder]: api/classy/generic/index.html#deriveDecoder[A,B](implicitev:classy.generic.derive.MkDecoder[A,B]):classy.core.Decoder[A,B]
[makeDecoder]: api/classy/generic/index.html#makeDecoder[A,B](implicitev:classy.generic.derive.MkDecoder[A,B]):classy.generic.derive.MkDecoder[A,B]
[MkDecoderWithOptions.typeCoproducts]: api/classy/generic/derive/MkDecoderWithOptions.html#typeCoproducts(fieldName:String,naming:classy.generic.derive.NamingStrategy):classy.generic.derive.MkDecoderWithOptions[A,B]
[ConfigDecoder]: api/classy/config/index.html#ConfigDecoder[A]=classy.core.Decoder[com.typesafe.config.Config,A]
