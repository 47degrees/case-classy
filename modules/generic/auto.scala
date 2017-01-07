/* -
 * Case Classy [classy-generic]
 */

package classy
package generic

import shapeless.Lazy

import core._
import derive._

package object auto {

  implicit def automaticallyMaterializeDecoder[A, B](
    implicit
    ev: Lazy[MkDecoder[A, B]]): Decoder[A, B] = ev.value.decoder
}
