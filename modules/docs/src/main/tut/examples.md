---
layout: page
title:  "Examples"
section: "examples"
position: 3
---

## Examples

More examples/explanations coming soon.

```tut:silent
case class Person(
  firstName: String,
  lastName: String,
  age: Option[Int])

case class MyConfig(
  people: List[Person])
```

We have a case class hierarchy that models a list of people. We'd like
to load the following configuration using Typesafe's Config library:

```
people = [

  { firstName : Augusta,
    lastName  : Ada },

  { firstName : Donald,
    lastName  : Knuth },

  { firstName : Grace,
    lastName  : Hopper }

]
```
```tut:invisible
val configString = """
people = [

  { firstName : Augusta,
    lastName  : Ada },

  { firstName : Donald,
    lastName  : Knuth },

  { firstName : Grace,
    lastName  : Hopper }

]
"""
```

We can use automatic decoder derivation to make this trivial.

```tut:silent
import classy.config._
import classy.generic.auto._

val decoder = ConfigDecoder[MyConfig]
```

No we can easily decode any Typesafe config object to our data types.

```tut:silent
// load configuration
import com.typesafe.config.ConfigFactory
val rawConfig = ConfigFactory.parseString(configString)

// then decode
import classy.core.DecodeError
val result0: Either[DecodeError, MyConfig] = decoder.decode(rawConfig)
```

Case classy also provides helpers so that you don't have to import
`com.typesafe.config` in most circumstances.

```tut:silent
// load direclty from string
val result1: Either[DecodeError, MyConfig] = decoder.fromString.decode(configString)

// load from classpath, shorcut for ConfigFactory.load
val result2: Either[DecodeError, MyConfig] = decoder.load()
```
