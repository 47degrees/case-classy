---
layout: page
title:  "Quick Start"
section: "quick start"
position: 1
---

# <a name="getting-started" class="anchor" href="#getting-started">Getting Started</a>

Case classy usage is straightforward. You can use the generic
facilities to derive config decoders automatically, you can write
decoders by hand, or you can combine approaches.

## <a name="tldr" class="anchor" href="#tldr">TL;DR</a>

You have the following case class hiearchy and you want to load it using out of
the box support.
```tut:silent
sealed trait Auth
case class User(username: String, password: String) extends Auth
case class Token(token: String) extends Auth

case class MyConfig(
  string: String,
  double: Option[Double],
  auth: Auth)
```

First derive a decoder that can load from `scala.collection.Map[String, String]`.
```tut:silent
import classy.stringMap._
import classy.generic._

// note: type StringMap = scala.collection.Map[String, String]
val decoder = deriveDecoder[StringMap, MyConfig]
```

Then use this decoder to load some data.
```tut:silent
//val config: Config = ConfigFactory.load() // load your config
//val result1: Either[DecodeError, MyConfig] = decoder(config)
```

If any errors occur during decoding, the `DecodeError` will be fully capture
everything that went wrong so you can easily debug the issue.

<br />

Alternatively, you can can use support for Typesafe Config.
```tut:silent
import classy.DecodeError
import classy.generic._
import classy.config._
import com.typesafe.config.{ Config, ConfigFactory }

val decoder = deriveDecoder[Config, MyConfig]
```
then load configuration with
```tut:silent
val config: Config = ConfigFactory.load() // load your config
val result1: Either[DecodeError, MyConfig] = decoder(config)
```
*or use this shortcut*
```tut:silent
val result2: Either[DecodeError, MyConfig] = decoder.load()
```
*or load a string directly*
```tut:silent
val result3: Either[DecodeError, MyConfig] =
  decoder.fromString("<<your config text>>")
```

***For a tiny example application, see the [tiny configured application](/case-classy/examples.html#configured-app)***
