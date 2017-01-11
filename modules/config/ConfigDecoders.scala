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
    def config     (path: String): ConfigDecoder[Config]        = instance(path)(_ getConfig _)
    def string     (path: String): ConfigDecoder[String]        = instance(path)(_ getString _)
    def number     (path: String): ConfigDecoder[Number]        = instance(path)(_ getNumber _)      //#=typesafe
    def boolean    (path: String): ConfigDecoder[Boolean]       = instance(path)(_ getBoolean _)
    def int        (path: String): ConfigDecoder[Int]           = instance(path)(_ getInt _)
    def long       (path: String): ConfigDecoder[Long]          = instance(path)(_ getLong _)
    def double     (path: String): ConfigDecoder[Double]        = instance(path)(_ getDouble _)

    def configList (path: String): ConfigDecoder[List[Config]]  = instance(path)(_ getConfigList _)
    def stringList (path: String): ConfigDecoder[List[String]]  = instance(path)(_ getStringList _)
    def numberList (path: String): ConfigDecoder[List[Number]]  = instance(path)(_ getNumberList _)  //#=typesafe
    def booleanList(path: String): ConfigDecoder[List[Boolean]] = instance(path)(_ getBooleanList _)
    def intList    (path: String): ConfigDecoder[List[Int]]     = instance(path)(_ getIntList _)
    def longList   (path: String): ConfigDecoder[List[Long]]    = instance(path)(_ getLongList _)
    def doubleList (path: String): ConfigDecoder[List[Double]]  = instance(path)(_ getDoubleList _)
    // format: ON

    @inline private[this] def instance[A: ClassTag](
      path: String)(f: (Config, String) => A): ConfigDecoder[A] =
      Decoder.instance(config =>
        try {
          f(config, path).right
        } catch {
          case e: ConfigException.Missing   => DecodeError.MissingPath(path).left
          case e: ConfigException.WrongType => DecodeError.WrongType(path, classTag[A].toString).left //#=typesafe
          //#+shocon
          case e: MatchError if e.getMessage == "null" =>
            DecodeError.MissingPath(path).left
          //#-shocon
          case other: Throwable             => DecodeError.AtPath(path, DecodeError.Underlying(other)).left
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

//#+shocon
// Compatibility for missing Shocon methods
// TODO: Contribute these back to Shocon?
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
    // Note: it's okay to return Scala's List in here here instead of
    // Java's List

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
