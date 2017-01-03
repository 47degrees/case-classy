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
  import ShoconCompat._ //#=shocon

  /** Typesafe config decoders that behave just like the underlying
    * counter part.  For the Shocon backend, some of these are
    * provided through a small compatibility layer.
    */
  object std {
    // format: OFF
    def config     (key: String): ConfigDecoder[Config]        = instance(key)(_ getConfig _)
    def string     (key: String): ConfigDecoder[String]        = instance(key)(_ getString _)
    def number     (key: String): ConfigDecoder[Number]        = instance(key)(_ getNumber _)      //#=typesafe
    def boolean    (key: String): ConfigDecoder[Boolean]       = instance(key)(_ getBoolean _)
    def int        (key: String): ConfigDecoder[Int]           = instance(key)(_ getInt _)
    def long       (key: String): ConfigDecoder[Long]          = instance(key)(_ getLong _)
    def double     (key: String): ConfigDecoder[Double]        = instance(key)(_ getDouble _)

    def configList (key: String): ConfigDecoder[List[Config]]  = instance(key)(_ getConfigList _)
    def stringList (key: String): ConfigDecoder[List[String]]  = instance(key)(_ getStringList _)
    def numberList (key: String): ConfigDecoder[List[Number]]  = instance(key)(_ getNumberList _)  //#=typesafe
    def booleanList(key: String): ConfigDecoder[List[Boolean]] = instance(key)(_ getBooleanList _)
    def intList    (key: String): ConfigDecoder[List[Int]]     = instance(key)(_ getIntList _)
    def longList   (key: String): ConfigDecoder[List[Long]]    = instance(key)(_ getLongList _)
    def doubleList (key: String): ConfigDecoder[List[Double]]  = instance(key)(_ getDoubleList _)
    // format: ON

    @inline private[this] def instance[A: ClassTag](
      key: String)(f: (Config, String) => A): ConfigDecoder[A] =
      Decoder.instance(config =>
        try {
          f(config, key).right
        } catch {
          case e: ConfigException.Missing   => DecodeError.MissingKey(key).left
          case e: ConfigException.WrongType => DecodeError.WrongType(key, classTag[A].toString).left //#=typesafe
          //#+shocon
          case e: MatchError if e.getMessage == "null" =>
            DecodeError.MissingKey(key).left
          //#-shocon
          case other: Throwable             => DecodeError.AtPath(key, DecodeError.Underlying(other)).left
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


  /** The backend implementation. Either "typesafe" or "shocon" */ //#=typesafe
  final val BACKEND: String = "typesafe"                           //#=typesafe
  /** The backend implementation. Either "typesafe" or "shocon" */ //#=shocon
  final val BACKEND: String = "shocon"                             //#=shocon
}

// Compatibility for missing Shocon methods
// TODO: Contribute these back to Shocon?
//#+shocon
private[config] object ShoconCompat {
  import eu.unicredit.shocon.{ Config => SConfig, Extractors }
  import scala.collection.generic.CanBuildFrom
  import Extractors._

  implicit def canBuildFromExtractor[T, F[_]](
    implicit
      ex: Extractor[T],
     cbf: CanBuildFrom[Nothing, T, F[T]]
  ): Extractor[F[T]] = {
    case SConfig.Array(seq) => (cbf() ++= seq.map(ex.apply(_))).result()
  }

  implicit class ShoconConfigCompatOps(val config: Config) extends AnyVal {
    // Note: it's safe to return Scala's List in here here instead of Java's List

    def getConfigList(path: String): List[Config] =
      config.getOrReturnNull[List[SConfig.Value]](path).map(Config.apply)

    def getBooleanList(path: String): List[Boolean] =
      config.getOrReturnNull[List[Boolean]](path)

    def getIntList(path: String): List[Int] =
      config.getOrReturnNull[List[Int]](path)

    def getLong(path: String): Long =
      config.getOrReturnNull[Long](path)

    def getLongList(path: String): List[Long] =
      config.getOrReturnNull[List[Long]](path)

    def getDoubleList(path: String): List[Double] =
      config.getOrReturnNull[List[Double]](path)
  }
}
//#-shocon
