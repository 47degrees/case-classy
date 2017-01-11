/* -
 * Case Classy [classy-generic]
 */

package classy

import core.Decoder
import generic.derive.MkDecoder

/** Provides automatic derivation of decoders for case class hierarchies.
  *
  * Available with the `"classy-generic"` module.
  */
package object generic {
  def deriveDecoder[A, B](implicit ev: MkDecoder[A, B]): Decoder[A, B] = ev.decoder
  def makeDecoder[A, B](implicit ev: MkDecoder[A, B]): MkDecoder[A, B] = ev
}
