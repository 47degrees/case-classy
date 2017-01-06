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

sealed abstract class MkDecoder[A, B] extends Serializable {

  def decoder: Decoder[A, B]
  def options: Options
  def withOptions(options: Options): MkDecoder[A, B]

  final def decoder(options: Options): Decoder[A, B] =
    withOptions(options).decoder

  final def renameFields(f: String => String): MkDecoder[A, B] =
    withOptions(options.copy(renameField = f))
}

object MkDecoder extends MkDecoderInstances2 {

  def apply[A, B](implicit ev: MkDecoder[A, B]): MkDecoder[A, B] = ev

  @inline private[derive] def instance[A, B](
    f: Options => Decoder[A, B]): MkDecoder[A, B] = new Instance(f)

  private final class Instance[A, B](
      f: Options => Decoder[A, B],
      val options: Options = Options.default
  ) extends MkDecoder[A, B] {

    override def withOptions(options0: Options): MkDecoder[A, B] =
      new Instance(f, options0)

    override def decoder: Decoder[A, B] = f(options)
  }

}

sealed trait MkDecoderInstances2 extends MkDecoderInstances1 { self: MkDecoder.type =>

  implicit def mkDecoderGeneric[A, B, L](
    implicit
    gen: LabelledGeneric.Aux[B, L],
    mkL: Lazy[MkDecoder[A, L]]
  ): MkDecoder[A, B] =
    instance(opt => mkL.value.decoder(opt).map(gen.from))

  implicit def mkDecoderHNil[A]: MkDecoder[A, HNil] =
    instance(_ => Decoder.const(HNil))

  implicit def mkDecoderCNil[A]: MkDecoder[A, CNil] =
    instance(_ => Decoder.fail(DecodeError.Identity))

}

sealed trait MkDecoderInstances1 extends MkDecoderInstances0 { self: MkDecoder.type =>

  implicit def mkDecoderHList[A, K <: Symbol, H, T <: HList](
    implicit
    key: Witness.Aux[K],
    readH: Lazy[Read[A, H]],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :: T] =
    instance(opt => (
      readH.value(opt.renameField(key.value.name))
      and
      mkT.value.decoder(opt)
      map { case (h, t) => field[K](h) :: t }
    ))

  implicit def mkDecoderCoproduct[A, K <: Symbol, H, T <: Coproduct](
    implicit
    key: Witness.Aux[K],
    decodeH: Lazy[Decoder[A, H]],
    nest: Read[A, A],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :+: T] =
    instance(opt => (
      opt.coproduct.decoder(decodeH.value, key.value.name).map(h => Inl(field[K](h)))
      or
      mkT.value.decoder(opt).map(t => Inr(t))
    ))
}

sealed trait MkDecoderInstances0 { self: MkDecoder.type =>

  implicit def mkDecoderHListDerived[A, K <: Symbol, H, T <: HList](
    implicit
    key: Witness.Aux[K],
    mkReadH: Lazy[MkRead[A, H]],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :: T] =
    instance(opt => (
      mkReadH.value(opt)(opt.renameField(key.value.name))
      and
      mkT.value.decoder(opt)
      map { case (h, t) => field[K](h) :: t }
    ))

  implicit def mkDecoderCoproductDerived[A, K <: Symbol, H, T <: Coproduct](
    implicit
    key: Witness.Aux[K],
    mkDecodeH: Lazy[MkDecoder[A, H]],
    nest: Read[A, A],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :+: T] =
    instance(opt => (
      opt.coproduct.decoder(mkDecodeH.value.decoder(opt), key.value.name).map(h => Inl(field[K](h)))
      or
      mkT.value.decoder(opt).map(t => Inr(t))
    ))
}

final class MkRead[A, B] private (f: Options => Read[A, B]) extends Serializable {
  def apply(options: Options): Read[A, B] = f(options)
}

object MkRead extends MkReadInstances0 {
  @inline private[derive] def instance[A, B](
    f: (Options) => Read[A, B]): MkRead[A, B] = new MkRead(f)
}

sealed trait MkReadInstances0 { self: MkRead.type =>

  implicit def mkReadNested[A, B](
    implicit
    nest: Read[A, A],
    mkDecoder: Lazy[MkDecoder[A, B]]
  ): MkRead[A, B] = instance(opt =>
    Read.defaultReadNested(nest, mkDecoder.value.decoder(opt)))

  implicit def mReadNestedSequenced[F[_], A, B](
    implicit
    Ft: Traversable[F],
    Fi: Indexed[F],
    read: Read[A, F[A]],
    mkDecoder: Lazy[MkDecoder[A, B]]
  ): MkRead[A, F[B]] = instance(opt =>
    Read.defaultReadNestedSequenced(Ft, Fi, read, mkDecoder.value.decoder(opt)))

  implicit def mkReadOption[A, B](
    implicit
    read: Read[A, B]
  ): MkRead[A, Option[B]] = instance(opt =>
    Read.defaultReadOption(read))
}

case class Options(
  renameField: String => String = (s => s),
  coproduct: CoproductStrategy = CoproductStrategy.default)
object Options {
  val default = Options()
}

abstract class CoproductStrategy extends Serializable {
  def decoder[A, B](
    decoder: Decoder[A, B],
    name: String
  )(implicit nest: Read[A, A]): Decoder[A, B]
}

object CoproductStrategy {

  object default extends CoproductStrategy {

    override def decoder[A, B](
      decoder: Decoder[A, B],
      name: String
    )(implicit nest: Read[A, A]): Decoder[A, B] =
      decoder.atPath(renameCoproduct(name))

    // todo: proper CamelCase -> snakeCase
    private[this] def renameCoproduct(in: String): String =
      if (in.length > 0) in.substring(0, 1).toLowerCase + in.substring(1)
      else in
  }
}
