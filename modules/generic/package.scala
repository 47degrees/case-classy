/* -
 * Case Classy [classy-generic]
 */

package classy

import core.Decoder
import generic.derive.MkDecoder

package object generic {
  def deriveDecoder[A, B](implicit ev: MkDecoder[A, B]): Decoder[A, B] = ev.decoder
}
