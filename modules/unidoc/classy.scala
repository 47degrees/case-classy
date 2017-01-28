/* -
 * Case Classy [docs]
 */

package classy

/** Case classy's core types.
  *
  * Available with the `"classy-core"` module.
  */
package object core {

  // Note: this file is special

  // The contents exist exclusively for documentation purposes
  // and to pull in implicits to the `classy` scope so that
  // unidoc includes various helper methods on the documentation
  // for our core types

  implicit def `proxy (for documentation) to cats.DecoderCatsOps`[A, B](
    decoder: Decoder[A, B]
  ): cats.DecoderCatsOps[A, B] = new cats.DecoderCatsOps[A, B](decoder)

  implicit def `proxy (for documentation) to config.ConfigDecoderOps`[A](
    decoder: config.ConfigDecoder[A]
  ): config.ConfigDecoderOps[A] = new config.ConfigDecoderOps[A](decoder)

}
