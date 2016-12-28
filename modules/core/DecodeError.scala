/* -
 * Case Classy [case-classy-core]
 */

package classy
package core

sealed abstract class DecodeError extends Product with Serializable {
  def &&(other: DecodeError): DecodeError.Aggregate = DecodeError.combine(this, other)
  def atPath(key: String): DecodeError.AtPath = DecodeError.AtPath(key, this)
}
object DecodeError extends DecodeErrorInstances {

  def combine(a: DecodeError, b: DecodeError): Aggregate = (a, b) match {
    case (manyA: Aggregate, manyB: Aggregate) ⇒
      Aggregate(manyA.head, manyA.tail ::: manyB.head :: manyB.tail)
    case (manyA: Aggregate, oneB) ⇒
      Aggregate(manyA.head, manyA.tail ::: oneB :: Nil)
    case (oneA, manyB: Aggregate) ⇒
      Aggregate(oneA, manyB.head :: manyB.tail)
    case (oneA, oneB) ⇒
      Aggregate(oneA, oneB :: Nil)
  }

  case class Aggregate(head: DecodeError, tail: List[DecodeError]) extends DecodeError {
    override def toString: String = s"""Aggregate(${(head :: tail).mkString(", ")})"""
  }
  case class AtPath(key: String, error: DecodeError) extends DecodeError

  sealed trait LeafDecodeError extends DecodeError
  case class MissingKey(key: String) extends LeafDecodeError
  case class WrongType(key: String, expected: String, got: Option[String] = None) extends LeafDecodeError
  case class Truncated(key: String, raw: String, result: String) extends LeafDecodeError
  case class Underlying(key: String, underlying: Throwable) extends LeafDecodeError

}

sealed trait DecodeErrorInstances {
  import wheel._

  implicit def eitherDecodeErrorApplicative: Applicative[Either[DecodeError, ?]] =
    new Applicative.EitherInstance[DecodeError] {
      override def map2[A, B, C](
        fa: Either[DecodeError, A],
        fb: Either[DecodeError, B])(
        f: (A, B) ⇒ C
      ): Either[DecodeError, C] = (fa, fb) match {
        case (Right(a), Right(b)) ⇒ f(a, b).right
        case (Left(za), Left(zb)) ⇒ (za && zb).left
        case (Left(za), _)        ⇒ za.left
        case (_, Left(zb))        ⇒ zb.left
      }
    }
}
