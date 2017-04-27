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

import java.io.InputStream //#=jvm
import java.util.Properties

package object stringMap {

  type StringMap     = scala.collection.Map[String, String]
  type JavaStringMap = java.util.Map[String, String]

  type StringMapDecoder[A] = Decoder[StringMap, A]
  object StringMapDecoder {
    def apply[A](implicit ev: StringMapDecoder[A]): StringMapDecoder[A] = ev
    def instance[A](f: StringMap => Either[DecodeError, A]): StringMapDecoder[A] =
      Decoder.instance(f)
  }

  implicit val stringMapReadString: Read[StringMap, String] =
    Read.instance(decoders.stringMapToString)

  implicit val stringMapReadNested: Read[StringMap, StringMap] =
    Read.instance(decoders.stringMapToStringMap)

  implicit val stringMapReadListString: Read[StringMap, List[String]] =
    Read.instance(decoders.stringMapToListString)

  implicit val stringMapReadListStringMap: Read[StringMap, List[StringMap]] =
    Read.instance(decoders.stringMapToListStringMap)

  val readStringMap: Read.From[StringMap] = Read.from[StringMap]

  implicit class StringMapDecoderOps[A](
    private val decoder: Decoder[StringMap, A]
  ) extends AnyVal {

    import scala.collection.convert.{ Wrappers => wrap }

    /** Converts this decoder to a decoder that parses a
      * `java.util.Map[String, String]` instead of a `Map[String, String]`
      *
      * @group stringMap
      */
    def fromJavaStringMap: Decoder[JavaStringMap, A] =
      decoder.mapInput(javaMap => wrap.JMapWrapper(javaMap))

    /** Converts this decoder to a decoder that parses
      * `java.util.Properties` instead of a `Map[String, String]`
      *
      * @group stringMap
      */
    def fromProperties: Decoder[Properties, A] =
      decoder.mapInput(properties => wrap.JPropertiesWrapper(properties))
  }

  //#+jvm
  implicit class PropertiesDecoderOps[A](
    private val decoder: Decoder[Properties, A]
  ) extends AnyVal {

    def fromInputStream: Decoder[InputStream, A] =
      decoder <<< decoders.inputStreamToProperties
  }
  //#-jvm
}
