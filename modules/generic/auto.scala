/* -
 * Case Classy [classy-generic]
 */

package classy
package generic

import core._
import derive._

package object auto {

  implicit def automaticallyMaterializeDecoder[A, B](
    implicit
    ev: MkDecoder[A, B]): Decoder[A, B] = ev.decoder
}
