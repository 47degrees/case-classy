---
layout: page
title:  "Derived"
section: "automatic decoders"
position: 2
---

## Deriving decoders automatically

Deriving decoders automatially is straightforward.
The [`classy-generic`](api/classy/generic/) module uses
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
to include the `classy-config` module in addition to `classy-generic`.

```tut:silent
import classy.core.DecodeError
import classy.generic._

import classy.config._
import com.typesafe.config.{ Config, ConfigFactory }
```

We'll use the `deriveDecoder` method that's part of `classy.generic`
to automatically summon a decoder for our case class hierarchy. We could
also use the `makeDecoder` method if we wanted to tweak some of the options
for our derived decoder.

```tut:silent
val decoder1: ConfigDecoder[MyConfig] = deriveDecoder[Config, MyConfig]
```

*Note: `ConfigDecoder[A]` is a type alias for `Decoder[Config, A]`*

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

### Coproduct Configuration

The default behavior for coproducts—sealed trait hierarchies—is to
simply try nested paths using a "camelCase" variation of the subtype's name.
We can use `makeDecoder` to configure this differently.

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
parameter to `typeCoproducts`.

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
