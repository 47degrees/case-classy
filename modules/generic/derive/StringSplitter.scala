/* -
 * Case Classy [classy-generic]
 */

package classy
package generic
package derive

import scala.annotation.switch
import scala.annotation.tailrec
//import twotails.mutualrec

/** A utility for splitting strings on word/phrase boundaries.
  * This supports the various [[NamingStrategy naming strategies]]
  */
object StringSplitter {
  import CharCat._

  def split(input: String): List[String] = {
    val chars = input.toCharArray
    val length = chars.length
    val end = length - 1

    (length: @switch) match {
      case 0 => List.empty
      case 1 => input :: Nil
      case _ =>
        val splits = CharCat(chars(0)) match {
          case AlphaUpper => firstUpper(chars, 1, end, 0 :: Nil)
          case Separator  => inSeparator(chars, 1, end, Nil)
          case _          => inLower(chars, 1, end, 0 :: Nil)
        }
        segments(input, length :: splits, Nil)
    }
  }

  @tailrec private[this] def segments(
    input: String, rem: List[Int], acc: List[String]
  ): List[String] =
    rem match {
      case first :: second :: tail => segments(input, tail, input.substring(second, first) :: acc)
      case _                       => acc
    }

  // @mutalrec
  private[this] def firstUpper(chars: Array[Char], i: Int, end: Int, acc: List[Int]): List[Int] =
    if (i >= end) acc else CharCat(chars(i)) match {
      case AlphaUpper => inUpper(chars, i + 1, end, acc, 1)
      case Numeric    => inUpper(chars, i + 1, end, acc, 0)
      case _          => inLower(chars, i + 1, end, acc)
    }

  // @mutalrec
  private[this] def inUpper(chars: Array[Char], i: Int, end: Int, acc: List[Int], offset: Int): List[Int] =
    if (i >= end) acc else CharCat(chars(i)) match {
      case AlphaUpper => inUpper(chars, i + 1, end, acc, 1)
      case Numeric    => inUpper(chars, i + 1, end, acc, 0)
      case Separator  => inSeparator(chars, i + 1, end, i :: acc)
      case _          => inLower(chars, i + 1, end, (i - offset) :: (i - offset) :: acc)
    }

  // @mutalrec
  private[this] def inLower(chars: Array[Char], i: Int, end: Int, acc: List[Int]): List[Int] =
    if (i >= end) acc else CharCat(chars(i)) match {
      case AlphaUpper => firstUpper(chars, i + 1, end, i :: i :: acc)
      case Separator  => inSeparator(chars, i + 1, end, i :: acc)
      case _          => inLower(chars, i + 1, end, acc)
    }

  // @mutalrec
  private[this] def inSeparator(chars: Array[Char], i: Int, end: Int, acc: List[Int]): List[Int] =
    if (i >= end) acc else CharCat(chars(i)) match {
      case AlphaUpper => firstUpper(chars, i + 1, end, i :: acc)
      case Separator  => inSeparator(chars, i + 1, end, acc)
      case _          => inLower(chars, i + 1, end, i :: acc)
    }

}

// format: OFF

/** A categorization of `char`. */
sealed abstract class CharCat extends Product with Serializable

object CharCat {

  /** An uppercase alphabetical character */
  case object AlphaUpper extends CharCat
  /** A lowercase alphabetical character */
  case object AlphaLower extends CharCat
  /** A numeric character */
  case object Numeric    extends CharCat
  /** A separator character, such as `_` */
  case object Separator  extends CharCat
  /** Any character not covered by the other categories */
  case object Other      extends CharCat

  /** Categorize a character */
  def apply(char: Char): CharCat = (char: @switch) match {

    case 'a' | 'b' | 'c' | 'd' | 'e' | 'f' |
         'g' | 'h' | 'i' | 'j' | 'k' | 'l' |
         'm' | 'n' | 'o' | 'p' | 'q' | 'r' |
         's' | 't' | 'u' | 'v' | 'w' | 'x' |
         'y' | 'z' => AlphaLower

    case 'A' | 'B' | 'C' | 'D' | 'E' | 'F' |
         'G' | 'H' | 'I' | 'J' | 'K' | 'L' |
         'M' | 'N' | 'O' | 'P' | 'Q' | 'R' |
         'S' | 'T' | 'U' | 'V' | 'W' | 'X' |
         'Y' | 'Z' => AlphaUpper

    case '0' | '1' | '2' | '3' | '4' | '5' |
         '6' | '7' | '8' | '9'
                   => Numeric

    case '_'       => Separator
    case  _        => Other
  }

}
