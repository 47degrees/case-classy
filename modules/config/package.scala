/* -
 * Case Classy [case-classy-config-typesafe]
 */

package classy

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import core.DecodeError
import core.Decoder
import core.Read

/** Provides support for [[https://github.com/typesafehub/config
  * Typesafe Config]]/[[https://github.com/unicredit/shocon Shocon
  * Config]].
  *
  * Available with the `"classy-config-typesafe"` and
  * `"classy-config-shocon"` modules. These modules and their
  * dependencies share the same classpath and cannot be used
  * simultaneously.
  *
  * ==Usage==
  *
  * Read instances are available with a wildcard import:
  * {{{
  *  import classy.config._
  * }}}
  *
  * This enables automatically derived decoders with
  * [[classy.generic]] as well as manual decoders:
  * {{{
  *  // decode a String from a Config at path "foo"
  *  val decodeFoo = readConfig[String]("foo")
  *
  *  // decode a List[Int] from a Config path "bar"
  *  val decodeBar = readConfig[List[Int]]("bar")
  * }}}
  *
  */
package object config {

  type ConfigDecoder[A] = Decoder[Config, A]
  object ConfigDecoder {
    def apply[A](implicit ev: ConfigDecoder[A]): ConfigDecoder[A] = ev
    def instance[A](f: Config => Either[DecodeError, A]): ConfigDecoder[A] =
      Decoder.instance(f)
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
      decoder(ConfigFactory.load())

    def load(loader: ClassLoader): Either[DecodeError, A] =
      decoder(ConfigFactory.load(loader))
    //#-typesafe

  }

  // implicit proxies for the defaults for generic derivation
  // format: OFF
  import ConfigDecoders.std._
  implicit val defaultConfigReadConfig      = Read.instance(config)
  implicit val defaultConfigReadString      = Read.instance(string)
  implicit val defaultConfigReadNumber      = Read.instance(number)      //#=typesafe
  implicit val defaultConfigReadBoolean     = Read.instance(boolean)
  implicit val defaultConfigReadInt         = Read.instance(int)
  implicit val defaultConfigReadLong        = Read.instance(long)
  implicit val defaultConfigReadDouble      = Read.instance(double)

  implicit val defaultConfigReadConfigList  = Read.instance(configList)
  implicit val defaultConfigReadStringList  = Read.instance(stringList)
  implicit val defaultConfigReadNumberList  = Read.instance(numberList)  //#=typesafe
  implicit val defaultConfigReadBooleanList = Read.instance(booleanList)
  implicit val defaultConfigReadIntList     = Read.instance(intList)
  implicit val defaultConfigReadLongList    = Read.instance(longList)
  implicit val defaultConfigReadDoubleList  = Read.instance(doubleList)
}
