/* -
 * Case Classy [classy-core]
 */

package classy
package core

/** An error that occurred while decoding data
  */
sealed abstract class DecodeError extends Product with Serializable {

  /** Create a conjunction between this error and another */
  def &&(other: DecodeError): DecodeError = DecodeError.and(this, other)

  /** Create a disjunction between this error and another */
  def ||(other: DecodeError): DecodeError = DecodeError.or(this, other)

  /** Indicate that this error occured while processing a value at `path` */
  def atPath(path: String): DecodeError.AtPath = DecodeError.AtPath(path, this)

  /** Indicate that this error occurred while processing a value at `index` */
  def atIndex(index: Int): DecodeError.AtIndex = DecodeError.AtIndex(index, this)

  /** Is this error an aggregation of other errors? */
  def isAggregate: Boolean = false
}

object DecodeError extends DecodeErrorInstances {

  /** A marker for decode errors that don't have any child errors
    */
  sealed trait LeafDecodeError extends DecodeError

  /** A value at a `path` was required but not found in the source data
    * structure
    */
  final case class MissingPath(path: String) extends LeafDecodeError

  /** A value was found at `path` but was the type was incorrect
    */
  final case class WrongType(path: String, expected: String, got: Option[String] = None) extends LeafDecodeError

  /** A value was found at `path` but was truncated during strict
    * decoding.
    *
    * This error typically occurs when strict decoding is used against
    * a backend implementation that normally defaults to lossy decoding
    * that truncates values (generally numeric).
    */
  final case class Truncated(path: String, raw: String, result: String) extends LeafDecodeError

  /** Qualifies a nested error indicating that it occured while decoding
    * a particular path within the source data structure
    */
  final case class AtPath(path: String, error: DecodeError) extends DecodeError

  /** Qualifies a nested error indicating that it occurred while decoding
    * a particular index within the source traversable data structure
    */
  final case class AtIndex(index: Int, error: DecodeError) extends DecodeError

  /** Indicates that an underlying (and generally unexpected) error occurred
    * while interacting with the source data structure.
    *
    * This typically shouldn't happen and this error may be removed in
    * future releases.
    */
  final case class Underlying(underlying: Throwable) extends LeafDecodeError {
    def canEqual(a: Any) = a.isInstanceOf[Underlying]
    override def equals(that: Any): Boolean =
      that match {
        case that: Underlying => that.canEqual(this) && this.hashCode == that.hashCode
        case _                => false
      }
    override def hashCode: Int =
      Option(underlying.getMessage).hashCode
  }

  /** An aggregation of multiple child errors that ocurred
    */
  sealed abstract class AggregateError extends DecodeError {

    /** The first child error that occurred */
    def head: DecodeError

    /** All of the other child errors that occurred */
    def tail: List[DecodeError]

    final override def isAggregate = true

    override def toString: String =
      s"""${getClass.getSimpleName}(${(head :: tail).mkString(", ")})"""
  }

  /** A conjunction of child errors.
    *
    * All child errors must be resolved for decoding to succeed.
    */
  final case class And(head: DecodeError, tail: List[DecodeError]) extends AggregateError
  object And {
    def apply(head: DecodeError, tail: DecodeError*): And = And(head, tail.toList)
  }

  /** A disjunction of child errors.
    *
    * One child error must be resolved for decoding to succeed.
    */
  final case class Or(head: DecodeError, tail: List[DecodeError]) extends AggregateError
  object Or {
    def apply(head: DecodeError, tail: DecodeError*): Or = Or(head, tail.toList)
  }

  /** An identity value that does nothing when `and` or `or`'d with
    * another error
    */
  private[classy] case object Identity extends DecodeError

  /** Create a conjunction of errors.
    *
    * If either error is already a conjunction then the contents are
    * unfolded before returning a single "flattened" result.
    */
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

  /** Create a disjunction of errors.
    *
    * If either error is already a disjunction then the contents are
    * unfolded before returning a single "flattened" result.
    */
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
}

private[core] sealed trait DecodeErrorInstances {
  import wheel._

  implicit def eitherDecodeErrorApplicative: Applicative[Either[DecodeError, ?]] =
    new instances.EitherApplicative[DecodeError](_ && _)
}
