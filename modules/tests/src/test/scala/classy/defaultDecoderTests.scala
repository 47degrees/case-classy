/* -
 * Case Classy [classy-tests]
 */

package classy

import predef._

import org.scalacheck._
import java.util.UUID

import testing._

class DefaultDecoderTests extends Properties("defaultDecoders") {
  import DefaultDecoderChecks._

  include(stringToBooleanProperties(decoders.stringToBoolean))
  include(stringToByteProperties(decoders.stringToByte))
  include(stringToShortProperties(decoders.stringToShort))
  include(stringToIntProperties(decoders.stringToInt))
  include(stringToLongProperties(decoders.stringToLong))
  include(stringToFloatProperties(decoders.stringToFloat))
  include(stringToDoubleProperties(decoders.stringToDouble))
  include(stringToUUIDProperties(decoders.stringToUUID))

  import DecoderChecks.positive

  implicit def ignoreReadNested[A]: Read[A, A] =
    Read.instance(_ => Decoder.instance(_.right))

  include(positive(decoders.stringToBoolean)(_.toString))
  include(positive(decoders.stringToByte)(_.toString))
  include(positive(decoders.stringToShort)(_.toString))
  include(positive(decoders.stringToInt)(_.toString))
  include(positive(decoders.stringToLong)(_.toString))
  include(positive(decoders.stringToFloat)(_.toString))
  include(positive(decoders.stringToDouble)(_.toString))
  include(positive(decoders.stringToUUID)(_.toString))

}

class DefaultReinterpretStringTests extends Properties("Read.Reinterpret") {
  import DefaultDecoderChecks._

  def reinterpret[A, B](implicit ev: Read.Reinterpret[A, B]): Decoder[A, B] = ev.decoder

  include(stringToBooleanProperties(reinterpret[String, Boolean]))
  include(stringToByteProperties(reinterpret[String, Byte]))
  include(stringToShortProperties(reinterpret[String, Short]))
  include(stringToIntProperties(reinterpret[String, Int]))
  include(stringToLongProperties(reinterpret[String, Long]))
  include(stringToFloatProperties(reinterpret[String, Float]))
  include(stringToDoubleProperties(reinterpret[String, Double]))
  include(stringToUUIDProperties(reinterpret[String, UUID]))

  import DecoderChecks.positive

  implicit def ignoreReadNested[A]: Read[A, A] =
    Read.instance(_ => Decoder.instance(_.right))

  include(positive(reinterpret[String, Boolean])(_.toString))
  include(positive(reinterpret[String, Byte])(_.toString))
  include(positive(reinterpret[String, Short])(_.toString))
  include(positive(reinterpret[String, Int])(_.toString))
  include(positive(reinterpret[String, Long])(_.toString))
  include(positive(reinterpret[String, Float])(_.toString))
  include(positive(reinterpret[String, Double])(_.toString))
  include(positive(reinterpret[String, UUID])(_.toString))

}
