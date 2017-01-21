/* -
 * Case Classy [classy-generic]
 */

package classy
package generic
package derive

import shapeless._
import shapeless.labelled.{ field, FieldType }

import core._
import core.wheel._

/** Captures the ability to automatically create a decoder given
  * [[Options]]
  */
final case class MkDecoder[A, B](run: Options => Decoder[A, B]) {
  def decoder(options: Options): Decoder[A, B] = run(options)
  def withOptions: MkDecoderWithOptions[A, B] = MkDecoderWithOptions(this, Options.default)
}

object MkDecoder extends MkDecoderInstances2

/** Captures the ability to automatically create a decoder.
  *
  * Additionally, this has builder-style methods to facilitate configuring
  * [[Options]] for the derived decoder.
  */
case class MkDecoderWithOptions[A, B](
    mkDecoder: MkDecoder[A, B],
    options: Options
) {
  def decoder: Decoder[A, B] = mkDecoder.decoder(options)

  /** Ajusts [[Options.naming]] */
  def naming(naming: NamingStrategy): MkDecoderWithOptions[A, B] =
    copy(options = options.copy(naming = naming))

  /** Adjusts [[Options.naming]], setting it to a namer backed by
    * function `f`
    */
  def renameFields(f: String => String): MkDecoderWithOptions[A, B] =
    copy(options = options.copy(naming = NamingStrategy.Basic(f)))

  /** Adjusts [[Options.coproduct]] */
  def coproduct(strategy: CoproductStrategy): MkDecoderWithOptions[A, B] =
    copy(options = options.copy(coproduct = strategy))
}

private[derive] sealed trait MkDecoderInstances2 extends MkDecoderInstances1 { self: MkDecoder.type =>

  implicit def mkDecoderGeneric[A, B, L](
    implicit
    gen: LabelledGeneric.Aux[B, L],
    mkL: Lazy[MkDecoder[A, L]]
  ): MkDecoder[A, B] =
    MkDecoder(opt => mkL.value.decoder(opt).map(gen.from))

  implicit def mkDecoderHNil[A]: MkDecoder[A, HNil] =
    MkDecoder(_ => Decoder.const(HNil))

  implicit def mkDecoderCNil[A]: MkDecoder[A, CNil] =
    MkDecoder(_ => Decoder.fail(DecodeError.Identity))

}

private[derive] sealed trait MkDecoderInstances1 extends MkDecoderInstances0 { self: MkDecoder.type =>

  implicit def mkDecoderHList[A, K <: Symbol, H, T <: HList](
    implicit
    key: Witness.Aux[K],
    readH: Lazy[Read[A, H]],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :: T] =
    MkDecoder(opt => (
      readH.value(opt.naming.name(key.value.name))
      and
      mkT.value.decoder(opt)
      map { case (h, t) => field[K](h) :: t }
    ))

  implicit def mkDecoderCoproduct[A, K <: Symbol, H, T <: Coproduct](
    implicit
    key: Witness.Aux[K],
    decodeH: Lazy[Decoder[A, H]],
    nest: Read[A, A],
    readString: Read[A, String],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :+: T] =
    MkDecoder(opt => (
      opt.coproduct.decoder(decodeH.value, key.value.name).map(h => Inl(field[K](h)))
      or
      mkT.value.decoder(opt).map(t => Inr(t))
    ))
}

private[derive] sealed trait MkDecoderInstances0 { self: MkDecoder.type =>

  implicit def mkDecoderHListDerived[A, K <: Symbol, H, T <: HList](
    implicit
    key: Witness.Aux[K],
    mkReadH: Lazy[MkRead[A, H]],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :: T] =
    MkDecoder(opt => (
      mkReadH.value(opt)(opt.naming.name(key.value.name))
      and
      mkT.value.decoder(opt)
      map { case (h, t) => field[K](h) :: t }
    ))

  implicit def mkDecoderCoproductDerived[A, K <: Symbol, H, T <: Coproduct](
    implicit
    key: Witness.Aux[K],
    mkDecodeH: Lazy[MkDecoder[A, H]],
    nest: Read[A, A],
    readString: Read[A, String],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :+: T] =
    MkDecoder(opt => (
      opt.coproduct.decoder(mkDecodeH.value.decoder(opt), key.value.name).map(h => Inl(field[K](h)))
      or
      mkT.value.decoder(opt).map(t => Inr(t))
    ))
}

final case class MkRead[A, B](f: Options => Read[A, B]) extends Serializable {
  def apply(options: Options): Read[A, B] = f(options)
}

object MkRead extends MkReadInstances0

private[derive] sealed trait MkReadInstances0 { self: MkRead.type =>

  implicit def mkReadNested[A, B](
    implicit
    nest: Read[A, A],
    mkDecoder: Lazy[MkDecoder[A, B]]
  ): MkRead[A, B] = MkRead(opt =>
    Read.defaultReadNested(nest, mkDecoder.value.decoder(opt)))

  implicit def mReadNestedSequenced[F[_], A, B](
    implicit
    Ft: Traversable[F],
    Fi: Indexed[F],
    read: Read[A, F[A]],
    mkDecoder: Lazy[MkDecoder[A, B]]
  ): MkRead[A, F[B]] = MkRead(opt =>
    Read.defaultReadNestedSequenced(Ft, Fi, read, mkDecoder.value.decoder(opt)))

  implicit def mkReadOption[A, B](
    implicit
    read: Read[A, B]
  ): MkRead[A, Option[B]] = MkRead(opt =>
    Read.defaultReadOption(read))
}

/** Options for the behavior of automatically derived decoders
  *
  * @param naming a [[NamingStrategy]] to control the mapping from field names
  * to the value [[core.Read read]] from the input.
  *
  * @param coproduct a [[CoproductStrategy]] govering how coproduct (sealed trait hierarchies)
  * types are differentiated and [[core.Read read]] from the input
  */
case class Options(
  naming: NamingStrategy = NamingStrategy.Identity,
  coproduct: CoproductStrategy = CoproductStrategy.Nested())

object Options {
  val default = Options()
}

/** An option that configures the behavior for automatically derived
  * decoders of coproduct types (sealed trait hierarchies).
  */
trait CoproductStrategy extends Serializable {

  /** Create a decoder for `A`
    *
    * @param decoder an existing decoder `A`
    * @param name the name of the type `A`
    * @param nest an implicit [[core.Read Read]] to permit calling of
    * [[core.Decoder.atPath atPath]], if desired
    */
  def decoder[A, B](
    decoder: Decoder[A, B],
    name: String
  )(implicit nest: Read[A, A], readString: Read[A, String]): Decoder[A, B]
}

object CoproductStrategy {

  /** A [[CoproductStrategy]] that decodes by reading the input at a nested
    * path based off the name of the type to decode
    *
    * @param naming a [[NamingStrategy]] for mapping the name of the type to
    * decode to the path name in the input
    */
  case class Nested(
      naming: NamingStrategy = NamingStrategy.CamelCase
  ) extends CoproductStrategy {
    override def decoder[A, B](
      decoder: Decoder[A, B],
      name: String
    )(implicit nest: Read[A, A], readString: Read[A, String]): Decoder[A, B] =
      decoder.atPath(naming.name(name))
  }

  /** A [[CoproductStrategy]] that decodes by reading a type field to determine
    * how to decode the nested value
    *
    * @param fieldName the name of the type field
    * @param naming a [[NamingStrategy]] to map the names of the decodable types
    * to the corresponding candidate values to be read from the type field
    */
  case class Typed(
      fieldName: String = "type",
      naming: NamingStrategy = NamingStrategy.CamelCase
  ) extends CoproductStrategy {
    def decoder[A, B](
      decoder: Decoder[A, B],
      name: String
    )(implicit nest: Read[A, A], readString: Read[A, String]): Decoder[A, B] =
      readString(fieldName) flatMap { value =>
        val requiredValue = naming.name(name)
        if (value == requiredValue) decoder
        else Decoder.fail(DecodeError.WrongType(value, requiredValue.some))
      }
  }

}

/** Captures the ability to take an input string name and output
  * a new name.
  */
trait NamingStrategy extends Serializable {
  def name(input: String): String
}

/** Companion containing several basic naming strategy implementations
  */
object NamingStrategy {
  import scala.collection.immutable.::

  /** The identity naming strategy that returns the input, unchanged
    */
  object Identity extends NamingStrategy {
    def name(input: String): String = input
  }

  /** A naming strategy backed by a single function */
  case class Basic(run: String => String) extends NamingStrategy {
    def name(input: String): String = run(input)
  }

  /** A naming strategy backed by two functions:
    * - One function to split an input string into parts
    * - Another function to join the parts into an output
    */
  class SplitJoinNamingStrategy(split: String => List[String])(join: List[String] => String) extends NamingStrategy {
    final def name(input: String): String = join(split(input))
  }

  /** A basic camel case naming strategy */
  object CamelCase extends SplitJoinNamingStrategy(StringSplitter.split)(
    _ match {
      case head :: tail => (decap(head) :: tail.map(cap)).mkString("")
      case Nil          => ""
    })

  /** A basic snake case (`Snake_case`) naming strategy that preserves
    * character case
    */
  object SnakeCase extends SplitJoinNamingStrategy(StringSplitter.split)(
    _.mkString("_"))

  /** A basic snake case (`SNAKE_CASE`) naming strategy that maps all
    * characters to uppercase
    */
  object UpperSnakeCase extends SplitJoinNamingStrategy(StringSplitter.split)(
    _.map(_.toUpperCase).mkString("_"))

  /** A basic snake case (`snake_case`) naming strategy that maps all
    * characters to lowercase
    */
  object LowerSnakeCase extends SplitJoinNamingStrategy(StringSplitter.split)(
    _.map(_.toLowerCase).mkString("_"))

  private[this] def cap(input: String): String = {
    var chars = input.toCharArray()
    chars(0) = Character.toUpperCase(chars(0))
    new String(chars)
  }

  private[this] def decap(input: String): String = {
    var chars = input.toCharArray()
    chars(0) = Character.toLowerCase(chars(0))
    new String(chars)
  }

}
