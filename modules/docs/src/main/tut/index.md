---
layout: home
title:  "Home"
section: "home"

technologies:
 - config: ["Typesafe Config", "Full support for Typesafe Config with the typesafe module"]
 - fp: ["Functional Programming", "Case classy follows functional programming paradigms with an emphasis on ease of use for beginners"]
 - scala: ["ScalaJS Ready", "Case classy fully supports ScalaJS when used with the Shocon module"]
---

Case classy is a tiny library to make it easy to decode untyped
structured data into case class hierarchies of your choosing. It's
completely modular, support Scala 2.11 and
2.12, [ScalaJS](https://www.scala-js.org) ready, and the core module
has _zero_ external dependencies.

[comment]: # (Start Replace)

<a name="modules"></a>
```scala
// required
libraryDependencies += "com.47deg" %% "classy-core"            % "0.4.0"

// at least one required
libraryDependencies += "com.47deg" %% "classy-config-typesafe" % "0.4.0"
libraryDependencies += "com.47deg" %% "classy-config-shocon"   % "0.4.0"

// optional
libraryDependencies += "com.47deg" %% "classy-generic"         % "0.4.0"
libraryDependencies += "com.47deg" %% "classy-cats"            % "0.4.0"
```

[comment]: # (End Replace)

The modules provide the following support:

 * `classy-core`: Basic set of configuration decoders and combinators. *required*
 * `classy-generic`: Automatic derivation for your case class
   hierarchies. *depends on [shapeless](https://github.com/milessabin/shapeless)*
 * `classy-config-typesafe`: Support for [Typesafe's Config](https://github.com/typesafehub/config) library.
 * `classy-config-shocon`: Support for the [Shocon](https://github.com/unicredit/shocon) config library.
 * `classy-cats`: Instances for [Cats](https://github.com/typelevel/cats).

All module support ScalaJS except `classy-config-typesafe`.

[comment]: # (Start Copyright)
# Copyright

Case Classy is designed and developed by 47 Degrees

Copyright (C) 2017 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)