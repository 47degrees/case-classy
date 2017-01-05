/* -
 * Case Classy [classy-core]
 */

package classy
package core

import wheel._

final class Read[A, B](f: String => Decoder[A, B]) extends Serializable {
  def apply(key: String): Decoder[A, B] = f(key)
}

object Read {
  def instance[A, B](f: (String) => Decoder[A, B]): Read[A, B] = new Read(f)

  implicit def defaultReadNested[A, B](
    implicit
    read: Read[A, A],
    decoder: Decoder[A, B]
  ): Read[A, B] = instance(key =>
    read(key) andThen decoder leftMap (_.atPath(key)))

  implicit def defaultReadNestedSequenced[F[_]: Traversable, A, B](
    implicit
    read: Read[A, F[A]],
    decoder: Decoder[A, B]
  ): Read[A, F[B]] = instance(key =>
    read(key) andThen decoder.sequence leftMap (_.atPath(key)))

  implicit def defaultReadOption[A, B](
    implicit
    read: Read[A, B]
  ): Read[A, Option[B]] = instance(key => read(key).optional)

}
