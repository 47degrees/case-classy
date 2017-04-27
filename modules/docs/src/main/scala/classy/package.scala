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
