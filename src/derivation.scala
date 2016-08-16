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
  * @author Andy Scott [47 Degrees]
  */
object derivation { // format: OFF

  /** Derive a decoder for type `A` from source data of type `C`.
    */
  def deriveDecoder[C, A: LabelledGeneric: Decoder[C, ?]]: Decoder[C, A] =
    Decoder[C, A]

  implicit def deriveDecoder0[C, A, L <: HList](
    implicit gen: LabelledGeneric.Aux[A, L], readFields: Lazy[Decoder[C, L]]
  ): Decoder[C, A] = Decoder(config ⇒
    Kleisli(readFields.value.run).map(gen.from).apply(config))

  // backend
  // Note: yyz prefix chosen arbitrarily to avoid name conflicts
  // ... it's also an arbitrary reference to the Rush song

  implicit def yyzReadHNil[C]: Decoder[C, HNil] =
    Decoder(_ ⇒ Validated.valid(HNil))

  implicit def yyzReadHCons[
    C, K <: Symbol, H: ReadValue[C, ?], T <: HList: Decoder[C, ?]
  ](implicit key: Witness.Aux[K]): Decoder[C, FieldType[K, H] :: T] =
    Decoder(config ⇒
      Apply[Res].map2(
        ReadValue[C, H].apply(config, key.value.name),
        Decoder[C, T].apply(config)
      )((head, tail) ⇒ field[K](head) :: tail))
}