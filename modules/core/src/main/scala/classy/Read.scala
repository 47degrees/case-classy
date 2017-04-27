/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package classy

import misc.wheel._

/** Read captures the ability to create a [[Decoder]] given a path string.
  *
  * @tparam A type `A` of created decoders
  * @tparam B type `B` of created decoders
  */
trait Read[A, B] extends Serializable {

  /** Create a decoder for the given `path`
    */
  def apply(path: String): Decoder[A, B]

  /** Create a decoder for the given `path`.
    *
    * This is an alias for [[apply]].
    */
  final def read(path: String): Decoder[A, B] = apply(path)
}

object Read extends ReadInstances0 {

  /** Implicitly summon a read
    */
  def apply[A, B](implicit ev: Read[A, B]): Read[A, B] = ev

  /** Implicitly summon a read and immediately apply to create a decoder
    */
  def apply[A, B](path: String)(implicit ev: Read[A, B]): Decoder[A, B] = ev(path)

  /** Summon [[Read]] instances with a fixed input type `A`
    */
  def from[A]: From[A] = new From[A]

  /** A helper for partially applying the types of [[Read]] for summoning reads
    * with a fixed input type `A`
    */
  class From[A] {

    /** Implicitly summon a read
      */
    def apply[B](implicit ev: Read[A, B]): Read[A, B] = ev

    /** Implicitly summon a read and immediately apply to create a decoder
      */
    def apply[B](path: String)(implicit ev: Read[A, B]): Decoder[A, B] = ev(path)
  }

  /** Creates a Read instance given a backing function `run`
    *
    * @param f the function from a path string to a decoder
    */
  def instance[A, B](run: (String) => Decoder[A, B]): Read[A, B] = Instance(run)

  /** The default implementation of [[Read]] backed by a function
    * `(String) => Decoder[A, B]`
    *
    * @param run the backing function
    */
  final case class Instance[A, B](run: (String) => Decoder[A, B]) extends Read[A, B] {
    override def apply(path: String): Decoder[A, B] = run(path)
  }

  /** A type class that wraps existing decoders in order to support
    * automatically composing decoders that decode to a broader range
    * of types
    *
    * This simpliy wraps a decoder and is used by [[Read]] via the
    * [[Read.defaultReadReinterpret]] implicit.
    */
  final class Reinterpret[A, B] private(val decoder: Decoder[A, B])

  object Reinterpret extends DefaultReinterpretStringInstances {

    /** Create a new [[Reinterpret]] instance for a [[Decoder]]
      */
    def instance[A, B](decoder: Decoder[A, B]): Reinterpret[A, B] =
      new Reinterpret[A, B](decoder)

    /** Create a new [[Reinterpret]] instance with a [[Decoder]] like
      * function
      */
    def instance[A, B](run: A => Either[DecodeError, B]): Reinterpret[A, B] =
      instance(Decoder.instance(run))
  }

}

private[classy] sealed trait ReadInstances0 { self: Read.type =>

  /** Provides read instances for nested decoder input types. Typically
    * this supports decoding of nested structures or data types.
    */
  implicit def defaultReadNested[A, B](
    implicit
      nest   : Read[A, A],
      decoder: Decoder[A, B]
  ): Read[A, B] = instance(path =>
    nest(path) >>> decoder.leftMap(_.atPath(path)))

  /** Provides read instances for traversable types such as `List`
    */
  implicit def defaultReadNestedSequenced[F[_]: Traversable: Indexed, A, B](
    implicit
      read   : Read[A, F[A]],
      decoder: Decoder[A, B]
  ): Read[A, F[B]] = instance(path =>
    read(path) >>> decoder.sequence.leftMap(_.atPath(path)))

  /** Provides read instances for `Option`
    */
  implicit def defaultReadOption[A, B](
    implicit
      read: Read[A, B]
  ): Read[A, Option[B]] = instance(path => read(path).optional)


  /** Provides read instances by automatically composing decoders
    * available via [[Reinterpret]]
    */
  implicit def defaultReadReinterpret[A, B, C](
    implicit
      reinterpret: Reinterpret[B, C],
      read       : Read[A, B]
  ): Read[A, C] = instance(path => read(path) >>> reinterpret.decoder)

}
