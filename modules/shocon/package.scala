/* -
 * Case Classy [case-classy-shocon]
 */

package classy

import com.typesafe.config.Config

import core.Read.{ instance ⇒ read }

package object shocon {
  type ConfigDecoder[A] = core.Decoder[Config, A]
  object ConfigDecoder {
    def apply[A](implicit ev: ConfigDecoder[A]): ConfigDecoder[A] = ev
  }

  // implicit proxies for the defaults for generic derivation
  // format: OFF
  import ConfigDecoders.std._

  implicit val defaultConfigReadConfig      = read(config)
  implicit val defaultConfigReadString      = read(string)
  //implicit val defaultConfigReadNumber      = read(number)
  implicit val defaultConfigReadBoolean     = read(boolean)
  implicit val defaultConfigReadInt         = read(int)
  //implicit val defaultConfigReadLong        = read(long)
  implicit val defaultConfigReadDouble      = read(double)

  //implicit val defaultConfigReadConfigList  = read(configList)
  implicit val defaultConfigReadStringList  = read(stringList)
  //implicit val defaultConfigReadNumberList  = read(numberList)
  //implicit val defaultConfigReadBooleanList = read(booleanList)
  //implicit val defaultConfigReadIntList     = read(intList)
  //implicit val defaultConfigReadLongList    = read(longList)
  //implicit val defaultConfigReadDoubleList  = read(doubleList)

}
