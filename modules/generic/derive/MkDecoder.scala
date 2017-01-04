/* -
 * Case Classy [case-classy-generic]
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

  implicit def mkDecoderGeneric[A, B, L <: HList](
    implicit
    gen: LabelledGeneric.Aux[B, L],
    mkL: Lazy[MkDecoder[A, L]]
  ): MkDecoder[A, B] =
    instance(mkL.value.decoder.map(gen.from))

}

/*
sealed abstract class UnconfiguredMkDecoder[A, B] extends Serializable {
  def configure: MkDecoder[A, B]
}

object UnconfiguredMkDecoder {
  def apply[A, B](implicit ev: UnconfiguredMkDecoder[A, B]): UnconfiguredMkDecoder[A, B] = ev

  implicit def unconfiguredMkDecoderHNil[A]: UnconfiguredMkDecoder[A, HNil] =
    new UnconfiguredMkDecoder[A, HNil] {
      def configure: MkDecoder[A, HNil] = MkDecoder.mkDecoderHNil
    }

  implicit def unconfiguredMkDecoderHList[A, K <: Symbol, H, T <: HList](
    implicit
    key: Witness.Aux[K],
    readH: Read[A, H],
    uMkT: UnconfiguredMkDecoder[A, T]
  ): UnconfiguredMkDecoder[A, FieldType[K, H] :: T] =

    new UnconfiguredMkDecoder[A, FieldType[K, H] :: T] {
      def configure: MkDecoder[A, FieldType[K, H] :: T] = {
        implicit val mkT: MkDecoder[A, T] = uMkT.configure
        MkDecoder.mkDecoderHList
      }
    }

}
*/
