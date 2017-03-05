/* -
 * Case Classy [classy-core]
 */

package classy
package core

object stringMap {

  type StringMap = scala.collection.Map[String, String]

  def stringMapDecodeString(key: String): Decoder[StringMap, String] =
    Decoder.instance(map =>
      map.get(key).toRight(DecodeError.AtPath(key, DecodeError.Missing)))

  def stringMapDecodeNested(key: String): Decoder[StringMap, StringMap] =
    Decoder.instance { map =>
      val prefix = s"$key."
      val filtered = map.toList
        .collect {
          case (path, value) if path.startsWith(prefix) =>
            (path.substring(prefix.length), value)
        }.toMap

      if (filtered.isEmpty)
        DecodeError.AtPath(key, DecodeError.Missing).left
      else
        filtered.right
    }

  implicit val stringMapReadString: Read[StringMap, String] =
    Read.instance(stringMapDecodeString)

  implicit val stringMapReadNested: Read[StringMap, StringMap] =
    Read.instance(stringMapDecodeNested)

  val readStringMap = Read.from[StringMap]
}


import scala.Predef._
object DOOFUS extends App {

  import java.util.UUID

  val data = Map(
    "a" -> "2000.0",
    "b" -> "true",
    "c" -> "123e4567-e89b-12d3-a456-426655440000"
  )

  import stringMap.{ readStringMap => read, _ }

  case class Doof(a: Float, b: Boolean, c: UUID)

  val decoder = (
    read[Float]("a") join
    read[Boolean]("b") join
    read[UUID]("c")
  ).map(Doof.tupled)

  println("> " + decoder(data))

}
