/* -
 * Case Classy [classy-core]
 */

package classy

import scala.annotation.tailrec

/** An error that occurred while decoding data
  */
sealed abstract class DecodeError extends Product with Serializable {

  /** Create a conjunction between this error and another */
  final def &&(other: DecodeError): DecodeError = DecodeError.and(this, other)

  /** Create a disjunction between this error and another */
  final def ||(other: DecodeError): DecodeError = DecodeError.or(this, other)

  /** Indicate that this error occured while processing a value at `path` */
  final def atPath(path: String): DecodeError.AtPath = DecodeError.AtPath(path, this)

  /** Indicate that this error occurred while processing a value at `index` */
  final def atIndex(index: Int): DecodeError.AtIndex = DecodeError.AtIndex(index, this)

  /** Is this error an aggregation of other errors? */
  def isAggregate: Boolean = false

  /** A pretty printed string representation of this error */
  final def toPrettyString: String = DecodeErrorPrinter.toPrettyString(this)
}

object DecodeError extends DecodeErrorInstances {

  /** A marker for decode errors that don't have any child errors
    */
  sealed trait LeafDecodeError extends DecodeError

  /** A value was required but not found in the source data structure
    */
  final case object Missing extends LeafDecodeError

  /** A value was present but was the type was incorrect
    */
  final case class WrongType(expected: String, got: Option[String] = None)
    extends LeafDecodeError

  /** A value was present and decoded but was truncated during the
    * decoding process.
    *
    * This error typically occurs when strict decoding is used against
    * a backend implementation that normally defaults to lossy decoding
    * that truncates values (generally numeric).
    */
  final case class Truncated(raw: String, result: String) extends LeafDecodeError

  /** Qualifies a nested error indicating that it occured while decoding
    * a particular path within the source data structure
    */
  final case class AtPath(path: String, error: DecodeError) extends DecodeError {

    /** Finds the deepest non-`AtPath` error by checking the type of
      * `error` and repeatedly calling this method on the `error` if
      * needed
      */
    @tailrec def deepError: DecodeError =
      error match {
        case error0: AtPath => error0.deepError
        case _              => error
      }
  }

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

    /** A list of errors containing [[head]] followed by [[tail]] */
    final def toList: List[DecodeError] = head :: tail

    final override def isAggregate = true
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
    case (_          , Identity   ) => a
    case (Identity   , _          ) => b
    case (And(ha, ta), And(hb, tb)) => And(ha, ta ::: hb :: tb)
    case (And(ha, ta), _          ) => And(ha, ta ::: b :: Nil)
    case (_          , And(hb, tb)) => And(a, hb :: tb)
    case _                          => And(a, b :: Nil)
  }

  /** Create a disjunction of errors.
    *
    * If either error is already a disjunction then the contents are
    * unfolded before returning a single "flattened" result.
    */
  def or(a: DecodeError, b: DecodeError): DecodeError = (a, b) match {
    case (_          , Identity  ) => a
    case (Identity   , _         ) => b
    case (Or(ha, ta) , Or(hb, tb)) => Or(ha, ta ::: hb :: tb)
    case (Or(ha, ta) , _         ) => Or(ha, ta ::: b :: Nil)
    case (_          , Or(hb, tb)) => Or(a, hb :: tb)
    case _                         => Or(a, b :: Nil)
  }

}

private[classy] sealed trait DecodeErrorInstances {
  import misc.wheel._

  implicit def eitherDecodeErrorApplicative: Applicative[Either[DecodeError, ?]] =
    new instances.EitherApplicative[DecodeError](_ && _)
}
