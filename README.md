# Case Classy
[![Build Status](https://api.travis-ci.org/47deg/case-classy.png?branch=master)](https://travis-ci.org/47deg/case-classy)

## Introduction

Case classy is a tiny framework to make it easy to decode untyped
structured data into case class hierarchies of your choosing. It's
completely modular, support Scala 2.11 and
2.12, [ScalaJS](https://www.scala-js.org) ready, and the core library
has _zero_ external dependencies.

The modules provide the following support:

 * `classy-core`: Basic set of configuration decoders and combinators. *required*
 * `classy-generic`: Automatic derivation for your case class hierarchies. *depends on [shapeless](https://github.com/milessabin/shapeless)*
 * `classy-config-typesafe`: Support for [Typesafe's Config](https://github.com/typesafehub/config) library.
 * `classy-config-shocon`: Support for the [Shocon](https://github.com/unicredit/shocon) config library. *depends on shocon*

All module support ScalaJS except `classy-config-typesafe`.

# License
The license can be found in [COPYING](COPYING).
