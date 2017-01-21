---
layout: page
title:  "Getting Started"
section: "getting started"
position: 1
---

## Getting Started

Case classy usage is straightforward. You can use the generic
facilities to derive config decoders automatically, you can write
decoders by hand, or you can combine approaches.

Generally speaking, you will want to add the following:

```tut:silent
import classy.core._
```

### Decoder basics

The main Classy type is [`Decoder`](api/classy/core/Decoder.html), which
captures the ability to decode data of type `A` to type `B`. It has a
single unimplemented method `apply` that either returns an error or
the result.

```tut:silent
trait Decoder[A, B] {
  def apply(a: A): Either[DecodeError, B]
}
```

Decoder has many other final methods to facilitate combining and
chaining decoders. For example, we can create a dummy decoder and
then map the result:

```tut:reset:invisible
import classy.core._
```

```tut:silent
// create a decoder for Int that always succeeds with the value
val decoder0: Decoder[Int, Int] = Decoder.instance(value => Right(value))

// create a new decoder by mapping the result to a string
val decoder1: Decoder[Int, String] = decoder0.map(_.toString)
```

And we can easily decode to `Option`:

```tut:silent
val decoder2: Decoder[Int, Option[String]] = decoder1.optional
```

Or decode a lists of inputs to a list of outputs:

```tut:silent
val decoder3: Decoder[List[Int], List[String]] = decoder1.sequence[List]
```

Decoding primitive types, such as Int, isn't tremendously useful on it's own.
What we really want to do is decode more complicated data structures, such as
a `Map` or a `Config` object.

```tut:silent
def decodeString(path: String): Decoder[Map[String, String], String] =
  Decoder.instance(_.get(path).toRight(DecodeError.Missing.atPath(path)))

val decodeA = decodeString("a")
val decodeB = decodeString("b")

val decodeAandB: Decoder[Map[String, String], (String, String)] =
  decodeA join decodeB

case class Res(a: String, b: String)

val decodeRes: Decoder[Map[String, String], Res] =
  decodeAandB map Res.tupled
```

Now we can decode some values and see what happens:

```tut
decodeRes(Map("hello" -> "world"))
decodeRes(Map("a" -> "foo"))
decodeRes(Map("a" -> "foo", "b" -> "bar"))
```

Above we created a helper method `decodeString` that let us easily create
a decoder for a path within our `Map`. This pattern is very common. In fact,
it's so ubiquitous that we introduce a type to capture this pattern:

```tut:silent:fail
class Read[A, B] {
  def apply(path: String): Decoder[A, B]
}
```

It's unlikely that you'll need to
use [`Read`](api/classy/core/Read.html) directly since Case Classy
provides most helpers and decoders in the standard modules.  However,
it's an important foundation for the library and occasionally shows up
in implicit errors if you're decoding types that aren't already
supported.
