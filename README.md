## Case Classy
[![Build Status](https://api.travis-ci.org/47deg/case-classy.png?branch=master)](https://travis-ci.org/47deg/case-classy)
[![codecov.io](http://codecov.io/github/47deg/case-classy/coverage.svg?branch=master)](http://codecov.io/github/47deg/case-classy?branch=master)

### Introduction

Case classy is a tiny framework to make it easy to decode untyped
structured data into case class hierarchies of your choosing. It's
completely modular, support Scala 2.11 and
2.12, [ScalaJS](https://www.scala-js.org) ready, and the core library
has _zero_ external dependencies.

```scala
// required
libraryDependencies += "com.fortysevendeg" %% "classy-core"            % "0.3.0"

// at least one required
libraryDependencies += "com.fortysevendeg" %% "classy-config-typesafe" % "0.3.0"
libraryDependencies += "com.fortysevendeg" %% "classy-config-shocon"   % "0.3.0"

// optional
libraryDependencies += "com.fortysevendeg" %% "classy-generic"         % "0.3.0"
libraryDependencies += "com.fortysevendeg" %% "classy-cats"            % "0.3.0"
```

The modules provide the following support:

 * `classy-core`: Basic set of configuration decoders and combinators. *required*
 * `classy-generic`: Automatic derivation for your case class
   hierarchies. *depends on [shapeless](https://github.com/milessabin/shapeless)*
 * `classy-config-typesafe`: Support for [Typesafe's Config](https://github.com/typesafehub/config) library.
 * `classy-config-shocon`: Support for the [Shocon](https://github.com/unicredit/shocon) config library.
 * `classy-cats`: Instances for [Cats](https://github.com/typelevel/cats).

All module support ScalaJS except `classy-config-typesafe`.

### Documentation

Documentation is available on the [website](https://47deg.github.io/case-classy/).

### Quick Example

```scala
import classy.generic._
import classy.config._

// Our configuration class hierarchy
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(length: Double, width: Double) extends Shape

case class MyConfig(
  someString: Option[String],
  shapes: List[Shape])

import com.typesafe.config.Config
val decoder1 = deriveDecoder[Config, MyConfig]
```

```scala
decoder1.fromString("shapes = []")
// res4: scala.util.Either[classy.core.DecodeError,MyConfig] = Right(MyConfig(None,List()))
```

### License
The license can be found in [COPYING].

[config tests]: /modules/tests-config/
[COPYING]: COPYING
