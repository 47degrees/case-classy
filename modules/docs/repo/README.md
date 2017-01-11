# Case Classy
[![Build Status](https://api.travis-ci.org/47deg/case-classy.png?branch=master)](https://travis-ci.org/47deg/case-classy)

## Introduction

Case classy is a tiny framework to make it easy to decode untyped
structured data into case class hierarchies of your choosing. It's
completely modular, support Scala 2.11 and
2.12, [ScalaJS](https://www.scala-js.org) ready, and the core library
has _zero_ external dependencies.

```scala
// required
libraryDependencies += "com.fortysevendeg" %% "classy-core"            % "0.2.0"

// at least one required
libraryDependencies += "com.fortysevendeg" %% "classy-config-typesafe" % "0.2.0"
libraryDependencies += "com.fortysevendeg" %% "classy-config-shocon"   % "0.2.0"

// optional
libraryDependencies += "com.fortysevendeg" %% "classy-generic"         % "0.2.0"
libraryDependencies += "com.fortysevendeg" %% "classy-cats"            % "0.2.0"
```

The modules provide the following support:

 * `classy-core`: Basic set of configuration decoders and combinators. *required*
 * `classy-generic`: Automatic derivation for your case class
   hierarchies. *depends on [shapeless](https://github.com/milessabin/shapeless)*
 * `classy-config-typesafe`: Support for [Typesafe's Config](https://github.com/typesafehub/config) library.
 * `classy-config-shocon`: Support for the [Shocon](https://github.com/unicredit/shocon) config library.
 * `classy-cats`: Instances for [Cats](https://github.com/typelevel/cats).

All module support ScalaJS except `classy-config-typesafe`.

# Usage

Usage is straightforward. You can use the generic facilities to derive
config decoders automatically, you can write decoders by hand, or you
can combine approaches.

**Deriving decoders automatically:**

```tut:silent
import classy.core.DecodeError
import classy.generic.auto._

// use typesafe/shocon bindings
import classy.config._

// Our configuration class hierarchy
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(length: Double, width: Double) extends Shape

case class MyConfig(
  someString: Option[String],
  shapes: List[Shape])

val decoder1 = ConfigDecoder[MyConfig]

val res: Either[DecodeError, MyConfig] =
  decoder1.load() // alias for decoder1.decode(ConfigFactory.load())
```

**Writing decoders by hand:**

_Readme example coming soon. See the [config tests](modules/tests-config/) for
the time being._

# License
The license can be found in [COPYING](COPYING).
