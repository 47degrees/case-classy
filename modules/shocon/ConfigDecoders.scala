/* -
 * Case Classy [case-classy-shocon]
 */

package classy
package shocon

import com.typesafe.config.Config
import com.typesafe.config.ConfigException

import scala.collection.JavaConverters._
//import scala.reflect.{ ClassTag, classTag }
import scala.reflect.ClassTag

import core._

object ConfigDecoders {

  /** Typesafe config decoders that behave just like the underlying counter part */
  object std {
    // format: OFF
    def config     (key: String): ConfigDecoder[Config]        = instance(key)(_ getConfig _)
    def string     (key: String): ConfigDecoder[String]        = instance(key)(_ getString _)
    //def number     (key: String): ConfigDecoder[Number]        = instance(key)(_ getNumber _)
    def boolean    (key: String): ConfigDecoder[Boolean]       = instance(key)(_ getBoolean _)
    def int        (key: String): ConfigDecoder[Int]           = instance(key)(_ getInt _)
    //def long       (key: String): ConfigDecoder[Long]          = instance(key)(_ getLong _)
    def double     (key: String): ConfigDecoder[Double]        = instance(key)(_ getDouble _)

    //def configList (key: String): ConfigDecoder[List[Config]]  = instance(key)(_ getConfigList _)
    def stringList (key: String): ConfigDecoder[List[String]]  = instance(key)(_ getStringList _)
    //def numberList (key: String): ConfigDecoder[List[Number]]  = instance(key)(_ getNumberList _)
    //def booleanList(key: String): ConfigDecoder[List[Boolean]] = instance(key)(_ getBooleanList _)
    //def intList    (key: String): ConfigDecoder[List[Int]]     = instance(key)(_ getIntList _)
    //def longList   (key: String): ConfigDecoder[List[Long]]    = instance(key)(_ getLongList _)
    //def doubleList (key: String): ConfigDecoder[List[Double]]  = instance(key)(_ getDoubleList _)
    // format: ON

    @inline private[this] def instance[A: ClassTag](
      key: String)(f: (Config, String) ⇒ A): ConfigDecoder[A] =
      Decoder.instance(config ⇒
        try {
          f(config, key).right
        } catch {
          case e: ConfigException.Missing ⇒ DecodeError.MissingKey(key).left
          //case e: ConfigException.WrongType ⇒ DecodeError.WrongType(key, classTag[A].toString).left
          case other: Throwable           ⇒ DecodeError.Underlying(key, other).left
        }
      )

    @inline private[this] implicit def convertLists[A, B](j: java.util.List[A]): List[B] =
      j.asScala.toList.map(_.asInstanceOf[B])
  }

  /** Typesafe config decoders that prevent narrowing of configuration
    * data during the loading process
    */
  object strict {
    // coming soon...
  }

}
