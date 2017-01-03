/* -
 * Case Classy [case-classy-config-typesafe]
 */

package classy

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import core.DecodeError
import core.Decoder
import core.Read
import core.Read.{ instance => read }

package object config {

  type ConfigDecoder[A] = Decoder[Config, A]
  object ConfigDecoder {
    def apply[A](implicit ev: ConfigDecoder[A]): ConfigDecoder[A] = ev
  }


  type ReadConfig[A] = Read[Config, A]
  object ReadConfig {
    def apply[A](path: String)(
      implicit read: ReadConfig[A]): ConfigDecoder[A] = read(path)
  }
  def readConfig[A](path: String)(
    implicit read: ReadConfig[A]): ConfigDecoder[A] = read(path)


  implicit class ConfigDecoderOps[A](val decoder: ConfigDecoder[A]) extends AnyVal {
    /** Converts this decoder to a decoder that parses a string instead of
      * a config object */
    def fromString: Decoder[String, A] =
      decoder compose Decoder.instance { data =>
        try {
          ConfigFactory.parseString(data).right
        } catch {
          case e: Throwable => DecodeError.Underlying(e).left
        }
      }

    //#+typesafe
    def load(): Either[DecodeError, A] =
      decoder.decode(ConfigFactory.load())

    def load(loader: ClassLoader): Either[DecodeError, A] =
      decoder.decode(ConfigFactory.load(loader))
    //#-typesafe

  }

  // implicit proxies for the defaults for generic derivation
  // format: OFF
  import ConfigDecoders.std._
  implicit val defaultConfigReadConfig      = read(config)
  implicit val defaultConfigReadString      = read(string)
  implicit val defaultConfigReadNumber      = read(number)      //#=typesafe
  implicit val defaultConfigReadBoolean     = read(boolean)
  implicit val defaultConfigReadInt         = read(int)
  implicit val defaultConfigReadLong        = read(long)
  implicit val defaultConfigReadDouble      = read(double)

  implicit val defaultConfigReadConfigList  = read(configList)
  implicit val defaultConfigReadStringList  = read(stringList)
  implicit val defaultConfigReadNumberList  = read(numberList)  //#=typesafe
  implicit val defaultConfigReadBooleanList = read(booleanList)
  implicit val defaultConfigReadIntList     = read(intList)
  implicit val defaultConfigReadLongList    = read(longList)
  implicit val defaultConfigReadDoubleList  = read(doubleList)
}
