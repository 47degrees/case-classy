package classy
package config

import com.typesafe.config._

import org.scalacheck._
import org.scalacheck.Prop._

import core._
import testing._

import scala.Predef._

class ConfigDecoderCompanionProperties extends Properties("ConfigDecoder(companion)") {

  implicit val cogenConfig = Cogen((_: Config).toString.hashCode.toLong)

  property("ConfigDecoder.instance consistent with Decoder.instance") =
    forAll { (f: Config => Either[DecodeError, String]) =>
      ConfigDecoder.instance[String](f) ?= Decoder.instance[Config, String](f)
    }

}
