/* -
 * Case Classy [case-classy-typesafe]
 */

package classy
package typesafe

import com.typesafe.config.Config
import com.typesafe.config.ConfigException

import scala.collection.JavaConverters._
import scala.reflect.{ ClassTag, classTag }

import core._

object TypesafeDecoders {

  /** Typesafe config decoders that behave just like the underlying counter part */
  object std {
    // format: OFF
    def config     (key: String): TypesafeDecoder[Config]        = instance(key)(_ getConfig _)
    def string     (key: String): TypesafeDecoder[String]        = instance(key)(_ getString _)
    def number     (key: String): TypesafeDecoder[Number]        = instance(key)(_ getNumber _)
    def boolean    (key: String): TypesafeDecoder[Boolean]       = instance(key)(_ getBoolean _)
    def int        (key: String): TypesafeDecoder[Int]           = instance(key)(_ getInt _)
    def long       (key: String): TypesafeDecoder[Long]          = instance(key)(_ getLong _)
    def double     (key: String): TypesafeDecoder[Double]        = instance(key)(_ getDouble _)

    def configList (key: String): TypesafeDecoder[List[Config]]  = instance(key)(_ getConfigList _)
    def stringList (key: String): TypesafeDecoder[List[String]]  = instance(key)(_ getStringList _)
    def numberList (key: String): TypesafeDecoder[List[Number]]  = instance(key)(_ getNumberList _)
    def booleanList(key: String): TypesafeDecoder[List[Boolean]] = instance(key)(_ getBooleanList _)
    def intList    (key: String): TypesafeDecoder[List[Int]]     = instance(key)(_ getIntList _)
    def longList   (key: String): TypesafeDecoder[List[Long]]    = instance(key)(_ getLongList _)
    def doubleList (key: String): TypesafeDecoder[List[Double]]  = instance(key)(_ getDoubleList _)
    // format: ON

    @inline private[this] def instance[A: ClassTag](
      key: String)(f: (Config, String) ⇒ A): TypesafeDecoder[A] =
      Decoder.instance(config ⇒
        try {
          f(config, key).right
        } catch {
          case e: ConfigException.Missing   ⇒ DecodeError.MissingKey(key).left
          case e: ConfigException.WrongType ⇒ DecodeError.WrongType(key, classTag[A].toString).left
          case other: Throwable             ⇒ DecodeError.Underlying(key, other).left
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
