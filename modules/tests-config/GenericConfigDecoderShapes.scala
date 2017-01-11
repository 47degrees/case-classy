package classy
package generic

import org.scalacheck._
import org.scalacheck.Prop._

import config._
import auto._
import core.Decoder
import core.DecodeError
import core.DecodeError._

import scala.Predef.ArrowAssoc

object GenericConfigDecoderShapes1 {

  object FlatFlat {
    case class Paddywhack(zapZip: ZapZip)
    sealed abstract class ZapZip
    case class Zap(zap: String) extends ZapZip
    case class Zip(zip: String) extends ZapZip
  }

  object FlatNest {
    case class Paddywhack(zapZip: ZapZip)
    sealed abstract class ZapZip
    object ZapZip {
      case class Zap(zap: String) extends ZapZip
      case class Zip(zip: String) extends ZapZip
    }
  }

  object NestFlat {
    case class Paddywhack(zapZip: Paddywhack.ZapZip)
    object Paddywhack {
      sealed abstract class ZapZip
      case class Zap(zap: String) extends ZapZip
      case class Zip(zip: String) extends ZapZip
    }
  }

  object NestNest {
    case class Paddywhack(zapZip: Paddywhack.ZapZip)
    object Paddywhack {
      sealed abstract class ZapZip
      object ZapZip {
        case class Zap(zap: String) extends ZapZip
        case class Zip(zip: String) extends ZapZip
      }
    }
  }

}

class GenericConfigDecoderShapes extends Properties("ConfigDecoder generic shapes") {

  // tests that we can organize our ADT a few different ways and we
  // can still derive decoders that are consistent with eachother (in
  // terms of errors, at least)

  import GenericConfigDecoderShapes1._

  val flatFlat = "ff" -> ConfigDecoder[FlatFlat.Paddywhack].fromString.map(_ => ())
  val flatNest = "fn" -> ConfigDecoder[FlatNest.Paddywhack].fromString.map(_ => ())
  val nestFlat = "nf" -> ConfigDecoder[NestFlat.Paddywhack].fromString.map(_ => ())
  val nestNest = "nn" -> ConfigDecoder[NestNest.Paddywhack].fromString.map(_ => ())

  val ios: List[(String, DecodeError)] = List(
    "          " -> MissingPath("zapZip"),
    "zapZip: {}" -> AtPath("zapZip", Or(MissingPath("zap"), MissingPath("zip")))
  )

  def check(
    name: String,
    x: Decoder[String, Unit],
    y: Decoder[String, Unit]
  ): Unit = ios.foreach { case (i, o) =>
      val rx = x.decode(i)
      val ry = y.decode(i)
      property(s"$name '$i'") = (rx ?= o.left) && (rx ?= ry)
  }

  List(flatFlat, flatNest, nestFlat, nestNest).combinations(2).foreach {
    case (nx, dx) :: (ny, dy) :: Nil => check(s"[$nx<=>$ny]", dx, dy)
    case _ =>
  }

}
