/* -
 * Case Classy [case-classy-config-typesafe]
 */

package classy
package config

import com.typesafe.config.Config
import com.typesafe.config.ConfigException

import scala.collection.JavaConverters._
import scala.reflect.{ ClassTag, classTag } //#=typesafe
import scala.reflect.ClassTag               //#=shocon

import core._

object ConfigDecoders {

  /** Typesafe config decoders that behave just like the underlying counter part */
  object std {
    // format: OFF
    def config     (key: String): ConfigDecoder[Config]        = instance(key)(_ getConfig _)
    def string     (key: String): ConfigDecoder[String]        = instance(key)(_ getString _)
    def number     (key: String): ConfigDecoder[Number]        = instance(key)(_ getNumber _)      //#=typesafe
    def boolean    (key: String): ConfigDecoder[Boolean]       = instance(key)(_ getBoolean _)
    def int        (key: String): ConfigDecoder[Int]           = instance(key)(_ getInt _)
    def long       (key: String): ConfigDecoder[Long]          = instance(key)(_ getLong _)        //#=typesafe
    def double     (key: String): ConfigDecoder[Double]        = instance(key)(_ getDouble _)

    def configList (key: String): ConfigDecoder[List[Config]]  = instance(key)(_ getConfigList _)  //#=typesafe
    def stringList (key: String): ConfigDecoder[List[String]]  = instance(key)(_ getStringList _)
    def numberList (key: String): ConfigDecoder[List[Number]]  = instance(key)(_ getNumberList _)  //#=typesafe
    def booleanList(key: String): ConfigDecoder[List[Boolean]] = instance(key)(_ getBooleanList _) //#=typesafe
    def intList    (key: String): ConfigDecoder[List[Int]]     = instance(key)(_ getIntList _)     //#=typesafe
    def longList   (key: String): ConfigDecoder[List[Long]]    = instance(key)(_ getLongList _)    //#=typesafe
    def doubleList (key: String): ConfigDecoder[List[Double]]  = instance(key)(_ getDoubleList _)  //#=typesafe
    // format: ON

    @inline private[this] def instance[A: ClassTag](
      key: String)(f: (Config, String) ⇒ A): ConfigDecoder[A] =
      Decoder.instance(config ⇒
        try {
          f(config, key).right
        } catch {
          case e: ConfigException.Missing   ⇒ DecodeError.MissingKey(key).left
          case e: ConfigException.WrongType ⇒ DecodeError.WrongType(key, classTag[A].toString).left //#=typesafe
          case other: Throwable             ⇒ DecodeError.AtPath(key, DecodeError.Underlying(other)).left
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
