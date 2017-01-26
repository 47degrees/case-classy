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

you have
```tut:silent
sealed trait Auth
case class User(username: String, password: String) extends Auth
case class Token(token: String) extends Auth

case class MyConfig(
  string: String,
  double: Option[Double],
  auth: Auth)
```
and you're using [Typesafe's Config](https://github.com/typesafehub/config)
```tut:silent
import classy.core._
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

If any errors occur during decoding, the `DecodeError` will be fully capture
everything that went wrong so you can easily debug the issue.

***For a tiny example application, see the [tiny configured application](/examples.html#configured-app)***
