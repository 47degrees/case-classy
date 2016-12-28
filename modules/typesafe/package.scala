/* -
 * Case Classy [case-classy-typesafe]
 */

package classy

import com.typesafe.config.Config

import core.Read.{ instance ⇒ read }

package object typesafe {
  type TypesafeDecoder[A] = core.Decoder[Config, A]
  object TypesafeDecoder {
    def apply[A](implicit ev: TypesafeDecoder[A]): TypesafeDecoder[A] = ev
  }

  // implicit proxies for the defaults for generic derivation
  // format: OFF
  import TypesafeDecoders.std._
  implicit val defaultTypesafeReadConfig      = read(config)
  implicit val defaultTypesafeReadString      = read(string)
  implicit val defaultTypesafeReadNumber      = read(number)
  implicit val defaultTypesafeReadBoolean     = read(boolean)
  implicit val defaultTypesafeReadInt         = read(int)
  implicit val defaultTypesafeReadLong        = read(long)
  implicit val defaultTypesafeReadDouble      = read(double)

  implicit val defaultTypesafeReadConfigList  = read(configList)
  implicit val defaultTypesafeReadStringList  = read(stringList)
  implicit val defaultTypesafeReadNumberList  = read(numberList)
  implicit val defaultTypesafeReadBooleanList = read(booleanList)
  implicit val defaultTypesafeReadIntList     = read(intList)
  implicit val defaultTypesafeReadLongList    = read(longList)
  implicit val defaultTypesafeReadDoubleList  = read(doubleList)
}
