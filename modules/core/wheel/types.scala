/* -
 * Case Classy [case-classy-core]
 */

package classy
package core
package wheel

// occasionally you reinvent the wheel

abstract class Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A ⇒ B): F[B]
}

object Functor {
  def apply[F[_]](implicit ev: Functor[F]): Functor[F] = ev
}

abstract class Applicative[F[_]] extends Functor[F] {
  def pure[A](a: A): F[A]
  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) ⇒ C): F[C]
}

object Applicative {
  def apply[F[_]](implicit ev: Applicative[F]): Applicative[F] = ev

  class EitherInstance[Z] extends Applicative[Either[Z, ?]] {
    def pure[A](a: A): Either[Z, A] = a.right

    def map2[A, B, C](fa: Either[Z, A], fb: Either[Z, B])(f: (A, B) ⇒ C): Either[Z, C] =
      (fa, fb) match {
        case (Right(a), Right(b)) ⇒ f(a, b).right
        case (Left(za), Left(zb)) ⇒ za.left
        case (Left(za), _)        ⇒ za.left
        case (_, Left(zb))        ⇒ zb.left
      }

    def map[A, B](fa: Either[Z, A])(f: A ⇒ B): Either[Z, B] = fa.map(f)
  }

  // this instance isn't used but is provided for thoroughness
  implicit def eitherApplicative[Z]: Applicative[Either[Z, ?]] =
    new EitherInstance[Z]
}

abstract class Traversable[F[_]] extends Functor[F] {
  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A ⇒ G[B]): G[F[B]]
  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] = traverse(fga)(ga ⇒ ga)
}

object Traversable {

  def apply[F[_]](implicit ev: Traversable[F]): Traversable[F] = ev

  implicit val listTraversable: Traversable[List] = new Traversable[List] {
    def map[A, B](fa: List[A])(f: A ⇒ B): List[B] = fa.map(f)
    def traverse[G[_], A, B](fa: List[A])(f: A ⇒ G[B])(implicit G: Applicative[G]): G[List[B]] =
      fa.foldLeft(G.pure(List.empty[B]))((acc, a) ⇒ G.map2(f(a), acc)(_ :: _))
  }
}
