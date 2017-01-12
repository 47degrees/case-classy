/* -
 * Case Classy [classy-core]
 */

package classy
package core

import wheel._

/** Read captures the ability to create a [[Decoder]] given a path string.
  *
  * @tparam A type A of created decoders
  * @tparam B type B of created decoders
  */
final class Read[A, B] private[Read] (f: String => Decoder[A, B]) extends Serializable {
  def apply(path: String): Decoder[A, B] = f(path)
}

object Read {

  /** Creates a Read instance given a backing function `f`
    *
    * @param f the function from a path string to a decoder
    */
  def instance[A, B](f: (String) => Decoder[A, B]): Read[A, B] = new Read(f)

  /** Provides read instances for nested decoder input types. Typically
    * this supports decoding of nested structures or data types.
    */
  implicit def defaultReadNested[A, B](
    implicit
    nest: Read[A, A],
    decoder: Decoder[A, B]
  ): Read[A, B] = instance(path =>
    nest(path) andThen decoder.leftMap(_.atPath(path)))

  /** Provides read instances for traversable types such as `List`
    */
  implicit def defaultReadNestedSequenced[F[_]: Traversable: Indexed, A, B](
    implicit
    read: Read[A, F[A]],
    decoder: Decoder[A, B]
  ): Read[A, F[B]] = instance(path =>
    read(path) andThen decoder.sequence.leftMap(_.atPath(path)))

  /** Provides read instances for `Option`
    */
  implicit def defaultReadOption[A, B](
    implicit
    read: Read[A, B]
  ): Read[A, Option[B]] = instance(path => read(path).optional)

}
