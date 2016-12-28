/* -
 * Case Classy [case-classy-core]
 */

package object classy {

  // shared
  private[classy] implicit class ToOptionOps[A](val a: A) extends AnyVal {
    def some: Option[A] = Some(a)
  }

  // shared
  private[classy] implicit class ToEitherOps[A](val a: A) extends AnyVal {
    def left[B]: Either[A, B] = Left(a)
    def right[B]: Either[B, A] = Right(a)
  }

  private[classy] implicit class EitherCompatOps[A, B](val either: Either[A, B]) extends AnyVal {
    // shared
    def leftMap[Z](f: A ⇒ Z): Either[Z, B] = either.left.map(f)
  }

}
