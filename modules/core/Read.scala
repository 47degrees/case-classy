/* -
 * Case Classy [classy-core]
 */

package classy
package core

import wheel._

final class Read[A, B](f: String => Decoder[A, B]) extends Serializable {
  def apply(path: String): Decoder[A, B] = f(path)
}

object Read {
  def instance[A, B](f: (String) => Decoder[A, B]): Read[A, B] = new Read(f)

  implicit def defaultReadNested[A, B](
    implicit
    nest: Read[A, A],
    decoder: Decoder[A, B]
  ): Read[A, B] = instance(path =>
    nest(path) andThen decoder.leftMap(_.atPath(path)))

  implicit def defaultReadNestedSequenced[F[_]: Traversable: Indexed, A, B](
    implicit
    read: Read[A, F[A]],
    decoder: Decoder[A, B]
  ): Read[A, F[B]] = instance(path =>
    read(path) andThen decoder.sequence.leftMap(_.atPath(path)))

  implicit def defaultReadOption[A, B](
    implicit
    read: Read[A, B]
  ): Read[A, Option[B]] = instance(path => read(path).optional)

}
