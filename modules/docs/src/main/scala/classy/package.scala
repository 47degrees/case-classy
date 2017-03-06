/* -
 * Case Classy [docs]
 */

package object classy {

  // Note: this file is special

  // The contents exist exclusively for documentation purposes
  // and to pull in implicits to the `classy` scope so that
  // unidoc includes various helper methods on the documentation
  // for our core types

  /** This implicit only exists to help generate better documentation for
    * [[Decoder]]
    */
  implicit def `implicit proxy for classy.cats`[A, B](
    decoder: Decoder[A, B]
  ): cats.DecoderCatsOps[A, B] = new cats.DecoderCatsOps[A, B](decoder)

  /** This implicit only exists to help generate better documentation for
    * [[Decoder]]
    */
  implicit def `implicit proxy for classy.config`[A](
    decoder: config.ConfigDecoder[A]
  ): config.ConfigDecoderOps[A] = new config.ConfigDecoderOps[A](decoder)

}
