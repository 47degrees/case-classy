---
layout: default
title:  "Map Support"
section: "usage"
---

Using case classy for decoding is easy.

```tut:silent
case class Person(
  firstName: String,
  lastName: String,
  age: Option[Int])
```

To decode case classes from Scala Maps, import the contents the map
support object and use the `deriveMapDecoder` helper.

```tut:silent
import classy._
import classy.map._

val loadPerson =
  deriveMapDecoder[Person]
```

Now we can easily load a person from Map data.

```tut:book
val data0 = Map(
  "firstName" -> "Lynne",
  "lastName" -> "Truss")

import cats.data.Xor
val lynneTruss: Xor[RootDecodeError, Person] = loadPerson(data0)
```


```tut:book
import cats.std.list._
lynneTruss.leftMap(_.show).swap.foreach(println)
```

Case classy supports nested types.

```tut:silent
case class Book(
  title: String,
  authors: List[Person])

val loadBook =
  deriveMapDecoder[Book]
```


```tut:book
val raw1 = Map(
  "title" -> "Eats shoots and leaves",
  "authors" -> List(Map(
    "firstName" -> "Someone",
    "lastName" -> "Joe"))
)

val res1: Xor[RootDecodeError, Book] = loadBook(raw1)
res1.leftMap(_.show).swap.foreach(println)
```
