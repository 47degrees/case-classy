---
layout: page
title:  "Examples"
section: "examples"
position: 3
---

## Decoding People

```tut:silent
// case class hierarchy and configuration text

case class Person(
  firstName: String,
  lastName: String,
  age: Option[Int])

case class MyConfig(
  people: List[Person])

val configString = """
  people = [
    { firstName : Augusta,
      lastName  : Ada },

    { firstName : Donald,
      lastName  : Knuth },

    { firstName : Grace,
      lastName  : Hopper }
  ]"""

// common imports

import com.typesafe.config.Config
import classy.config._

// writing a decoder by hand
val decodePerson: ConfigDecoder[Person] =
  readConfig[String]("firstName").join(
  readConfig[String]("lastName")).join(
  readConfig[Option[Int]]("age")).map(Person.tupled)
val manualDecoder = readConfig[List[Config]]("people") andThen decodePerson.sequence

// deriving a decoder automatically
import classy.generic._
val derivedDecoder = deriveDecoder[Config, MyConfig]

// decoding the configuration

import com.typesafe.config.ConfigFactory
import classy.core.DecodeError

val rawConfig = ConfigFactory.parseString(configString)
```
```tut:book
manualDecoder(rawConfig)
derivedDecoder(rawConfig)
```
