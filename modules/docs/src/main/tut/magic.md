---
layout: page
title:  "Automagic"
section: "automatic decoders"
position: 2
---

## Deriving decoders automatically

Deriving decoders automatially isn't actually magic.
The [`classy-generic`](api/classy/generic/) module uses
[shapeless](https://github.com/milessabin/shapeless) to
automatically create decoders for product and coproduct types
such as case class hierarchies.

```tut:silent
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(length: Double, width: Double) extends Shape

case class MyConfig(
  someString: Option[String],
  shapes: List[Shape])
```

We'll derive decoders for `Config` objects, so we need to include
the `classy-config` module in addition to `classy-generic`.

```tut:silent
import classy.core.DecodeError
import classy.generic._

import classy.config._
import com.typesafe.config.{ Config, ConfigFactory }
```

We'll use the `deriveDecoder` method that's part of `classy.generic`.

```tut:silent
val decoder1 = deriveDecoder[Config, MyConfig]
```

Now we can decode our configs!

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
