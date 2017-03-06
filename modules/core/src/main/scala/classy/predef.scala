/* -
 * Case Classy [classy-core]
 */

package classy


private[classy] object predef {

  type <:<[A, B] = scala.Predef.<:<[A, B]
  implicit def $conforms[A]: A <:< A = scala.Predef.$conforms[A]

  private[classy] implicit class ToOptionOps[A](val a: A) extends AnyVal {
    def some: Option[A] = Some(a)
  }

  private[classy] implicit class ToEitherOps[A](val a: A) extends AnyVal {
    def left[B]: Either[A, B] = Left(a)
    def right[B]: Either[B, A] = Right(a)
  }

  private[classy] implicit class EitherCompatOps[A, B](val either: Either[A, B]) extends AnyVal {
    def leftMap[Z](f: A => Z): Either[Z, B] = either.left.map(f)
    def map[C](f: B => C): Either[A, C] = either.right.map(f)                    //#=2.11
    def flatMap[C](f: B => Either[A, C]): Either[A, C] = either.right.flatMap(f) //#=2.11
    def toOption: Option[B] = either.fold(_ => None, v => Some(v))               //#=2.11
    def getOrElse[BB >: B](or: => BB): BB = either.right.getOrElse(or)           //#=2.11
  }

  type Traversable[F[_]] = misc.wheel.Traversable[F]
  type Indexed[F[_]]     = misc.wheel.Indexed[F]

}
