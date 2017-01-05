/* -
 * Case Classy [classy-generic]
 */

package classy
package generic
package derive

import shapeless._
import shapeless.labelled.{ field, FieldType }

import core._

sealed abstract class MkDecoder[A, B] extends Serializable {
  def decoder: Decoder[A, B]
}

object MkDecoder {

  @inline private[this] def instance[A, B](instance: Decoder[A, B]): MkDecoder[A, B] =
    new MkDecoder[A, B] {
      override def decoder: Decoder[A, B] = instance
    }

  implicit def mkDecoderHNil[A]: MkDecoder[A, HNil] =
    instance(Decoder.const(HNil))

  implicit def mkDecoderHList[A, K <: Symbol, H, T <: HList](
    implicit
    key: Witness.Aux[K],
    readH: Lazy[Read[A, H]],
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :: T] =
    instance((readH.value(key.value.name) and mkT.value.decoder).map {
      case (h, t) => field[K](h) :: t
    })

  implicit def mkDecoderCNil[A]: MkDecoder[A, CNil] =
    instance(Decoder.fail(DecodeError.Identity))

  implicit def mkDecoderCoproduct[A, K <: Symbol, H, T <: Coproduct](
    implicit
    key: Witness.Aux[K],
    decodeH: Lazy[Decoder[A, H]],
    nest: Read[A, A],
    strategy: CoproductStrategy,
    naming: CoproductNaming,
    mkT: Lazy[MkDecoder[A, T]]
  ): MkDecoder[A, FieldType[K, H] :+: T] =
    instance(
      strategy.decoder(decodeH.value, key.value.name).map(h => Inl(field[K](h)))
        or
        mkT.value.decoder.map(t => Inr(t)))

  implicit def mkDecoderGeneric[A, B, L](
    implicit
    gen: LabelledGeneric.Aux[B, L],
    mkL: Lazy[MkDecoder[A, L]]
  ): MkDecoder[A, B] =
    instance(mkL.value.decoder.map(gen.from))

}

trait CoproductNaming {
  def renameCoproduct(in: String): String
}

object CoproductNaming {
  implicit object DefaultCoproductNaming extends CoproductNaming {
    // TODO: make this a robust CamelCase -> snakeCase
    override def renameCoproduct(in: String): String =
      if (in.length > 0) in.substring(0, 1).toLowerCase + in.substring(1)
      else in
  }
}

abstract class CoproductStrategy extends Serializable {
  def decoder[A, B](
    decoder: Decoder[A, B],
    name: String
  )(implicit nest: Read[A, A], naming: CoproductNaming): Decoder[A, B]
}

object CoproductStrategy {

  implicit object DefaultCoproductStrategy extends CoproductStrategy {
    override def decoder[A, B](
      decoder: Decoder[A, B],
      name: String
    )(implicit nest: Read[A, A], naming: CoproductNaming): Decoder[A, B] =
      decoder.atPath(naming.renameCoproduct(name))
  }
}
