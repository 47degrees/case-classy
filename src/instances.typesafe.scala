/* -
 * Case Classy [case-classy-typesafe]
 */

package classy

import cats.data._

import com.typesafe.config.Config

// TODO

object typesafe {
  import DecodeError._

  type TypesafeDecoder[A] = Decoder[Config, A]

  def deriveTypesafeDecoder[A: TypesafeDecoder] = Decoder[Config, A]
}
