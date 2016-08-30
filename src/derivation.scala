/* -
 * Case Classy [case-classy]
 */

package classy

import shapeless._
import shapeless.labelled.{ field, FieldType }

import cats._
import cats.data._
import cats.implicits._

/** Semi-automatic derivation of case class "readers" for configuration data.
  *
  * @author Andy Scott
  */
trait ClassyDerivation { // format: OFF

  /** Derive a decoder for type `A` from source data of type `C`.
    */
  def deriveDecoderV[C, A: LabelledGeneric: DecoderV[C, ?]]: DecoderV[C, A] =
    DecoderV[C, A]

  implicit def deriveDecoderV0[C, A, L <: HList](
    implicit gen: LabelledGeneric.Aux[A, L], readFields: Lazy[DecoderV[C, L]]
  ): DecoderV[C, A] = DecoderV(config ⇒
    Kleisli(readFields.value.run).map(gen.from).apply(config))

  // backend
  // Note: yyz prefix chosen arbitrarily to avoid name conflicts
  // ... it's also an arbitrary reference to the Rush song

  implicit def yyzReadHNil[C]: DecoderV[C, HNil] =
    DecoderV(_ ⇒ Validated.valid(HNil))

  implicit def yyzReadHCons[
    C, K <: Symbol, H: ReadValue[C, ?], T <: HList: DecoderV[C, ?]
  ](implicit key: Witness.Aux[K]): DecoderV[C, FieldType[K, H] :: T] =
    DecoderV(config ⇒
      Apply[ResV].map2(
        ReadValue[C, H].apply(config, key.value.name),
        DecoderV[C, T].apply(config)
      )((head, tail) ⇒ field[K](head) :: tail))
}
