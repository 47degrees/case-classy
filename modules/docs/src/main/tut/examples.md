---
layout: page
title:  "Examples"
section: "examples"
position: 3
---

# Examples

* [Decoding People](#decoding-people)

  Demonstrates writing decoders by hand compared to automatic decoders

* [Configured Application](#configured-app)

  A trivial application that loads config on boot


## <a name="decoding-people" class="anchor" href="#decoding-people">Decoding People</a>

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
import classy.DecodeError

val rawConfig = ConfigFactory.parseString(configString)
```
```tut:book
manualDecoder(rawConfig)
derivedDecoder(rawConfig)
```

## <a name="configured-app" class="anchor" href="#configured-app">Configured Application</a>
```tut:reset:silent
import classy.generic._
import classy.config._
import com.typesafe.config.Config

object QuickApplication {

  // we're going to load a list of people and some trivial actions from configuration data
  case class Options(
    people : List[Person],
    actions: List[Action])

  case class Person(
    firstName: String,
    lastName: String)

  sealed abstract class Action extends Product with Serializable
  case class Dance(dance: String) extends Action
  case class Sing(song: String) extends Action
  case class Shout(expletive: String) extends Action

  // main method that might abort if the configuration is invalid
  def main(args: Array[String]): Unit = {
    val decoder = deriveDecoder[Config, Options]

    // decoder.load() is just a shortcut for Typesafe's ConfigFactory.load()
    decoder.load() match {

      case Left(error) =>
        // loading the config failed! this will print a reasonable error message
        System.err.println("config error: " + error)
        System.exit(-1)

      case Right(options) =>
        main(options)
    }
  }

  // main method that is only called with valid config
  def main(options: Options): Unit = {

    // assembly messages of everyone doing every action
    val res: List[String] = for {
      person <- options.people
      action <- options.actions
      text    = action match {
        case Dance(dance) => s"dances $dance"
        case Sing(song)   => s"sings $song"
        case Shout(word)  => s"shouts $word at the top of his/her lungs"
      }

    } yield s"$person $text"

    // end of the world: print
    res.foreach(println)
  }

}

```
