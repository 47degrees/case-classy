/* -
 * Case Classy [classy-core]
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