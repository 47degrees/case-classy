/* -
 * Case Classy [classy-core]
 */

package classy
package core

sealed abstract class DecodeError extends Product with Serializable {
  def &&(other: DecodeError): DecodeError = DecodeError.and(this, other)
  def ||(other: DecodeError): DecodeError = DecodeError.or(this, other)

  def atPath(key: String): DecodeError.AtPath = DecodeError.AtPath(key, this)
  def atIndex(index: Int): DecodeError.AtIndex = DecodeError.AtIndex(index, this)
}
object DecodeError extends DecodeErrorInstances {

  def and(a: DecodeError, b: DecodeError): DecodeError = (a, b) match {
    case (anyA, Identity) => anyA
    case (Identity, anyB) => anyB
    case (manyA: And, manyB: And) =>
      And(manyA.head, manyA.tail ::: manyB.head :: manyB.tail)
    case (manyA: And, oneB) =>
      And(manyA.head, manyA.tail ::: oneB :: Nil)
    case (oneA, manyB: And) =>
      And(oneA, manyB.head :: manyB.tail)
    case (oneA, oneB) =>
      And(oneA, oneB :: Nil)
  }

  def or(a: DecodeError, b: DecodeError): DecodeError = (a, b) match {
    case (anyA, Identity) => anyA
    case (Identity, anyB) => anyB
    case (manyA: Or, manyB: Or) =>
      Or(manyA.head, manyA.tail ::: manyB.head :: manyB.tail)
    case (manyA: Or, oneB) =>
      Or(manyA.head, manyA.tail ::: oneB :: Nil)
    case (oneA, manyB: Or) =>
      Or(oneA, manyB.head :: manyB.tail)
    case (oneA, oneB) =>
      Or(oneA, oneB :: Nil)
  }

  case class And(head: DecodeError, tail: List[DecodeError]) extends DecodeError {
    override def toString: String = s"""And(${(head :: tail).mkString(", ")})"""
  }

  object And {
    def apply(head: DecodeError, tail: DecodeError*): And = And(head, tail.toList)
  }

  case class Or(head: DecodeError, tail: List[DecodeError]) extends DecodeError {
    override def toString: String = s"""Or(${(head :: tail).mkString(", ")})"""
  }

  object Or {
    def apply(head: DecodeError, tail: DecodeError*): Or = Or(head, tail.toList)
  }

  case class AtPath(key: String, error: DecodeError) extends DecodeError
  case class AtIndex(index: Int, error: DecodeError) extends DecodeError

  sealed trait LeafDecodeError extends DecodeError
  case class MissingKey(key: String) extends LeafDecodeError
  case class WrongType(key: String, expected: String, got: Option[String] = None) extends LeafDecodeError
  case class Truncated(key: String, raw: String, result: String) extends LeafDecodeError
  case class Underlying(underlying: Throwable) extends LeafDecodeError {
    def canEqual(a: Any) = a.isInstanceOf[Underlying]
    override def equals(that: Any): Boolean =
      that match {
        case that: Underlying => that.canEqual(this) && this.hashCode == that.hashCode
        case _                => false
      }
    override def hashCode: Int =
      Option(underlying.getMessage).hashCode
  }
  private[classy] case object Identity extends DecodeError

}

sealed trait DecodeErrorInstances {
  import wheel._

  implicit def eitherDecodeErrorApplicative: Applicative[Either[DecodeError, ?]] =
    new Applicative.EitherInstance[DecodeError] {
      override def map2[A, B, C](
        fa: Either[DecodeError, A],
        fb: Either[DecodeError, B])(
        f: (A, B) => C
      ): Either[DecodeError, C] = (fa, fb) match {
        case (Right(a), Right(b)) => f(a, b).right
        case (Left(za), Left(zb)) => (za && zb).left
        case (Left(za), _)        => za.left
        case (_, Left(zb))        => zb.left
      }
    }
}
