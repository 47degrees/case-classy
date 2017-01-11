/* -
 * Case Classy [classy-core]
 */

package classy
package core.wheel

/*
 * A small set of type classes (with partial implementations) needed
 * for some of the internal operations of this library.
 *
 * These aren't intended to be used as foundations for any code
 * outside of this library.
 */

trait Functor[F[_]] extends Serializable {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object Functor {
  def apply[F[_]](implicit ev: Functor[F]): Functor[F] = ev
}

trait Applicative[F[_]] extends Functor[F] {
  def pure[A](a: A): F[A]
  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]
}

object Applicative {
  def apply[F[_]](implicit ev: Applicative[F]): Applicative[F] = ev
  implicit val listApplicative: Applicative[List] = instances.ListInstance
}

trait Traversable[F[_]] extends Functor[F] {
  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] = traverse(fga)(ga => ga)
}

object Traversable {
  def apply[F[_]](implicit ev: Traversable[F]): Traversable[F] = ev
  implicit val listTraversable: Traversable[List] = instances.ListInstance
}

trait Indexed[F[_]] extends Serializable {
  def indexed[A](fa: F[A]): F[(Int, A)]
}

object Indexed {
  implicit val listIndexed: Indexed[List] = instances.ListInstance
}

object instances {

  final class EitherApplicative[Z] private[core] (fz: (Z, Z) => Z) extends Applicative[Either[Z, ?]] {
    def pure[A](a: A): Either[Z, A] = a.right
    def map2[A, B, C](fa: Either[Z, A], fb: Either[Z, B])(f: (A, B) => C): Either[Z, C] =
      (fa, fb) match {
        case (Right(a), Right(b)) => f(a, b).right
        case (Left(za), Left(zb)) => fz(za, zb).left
        case (Left(za), _)        => za.left
        case (_, Left(zb))        => zb.left
      }
    def map[A, B](fa: Either[Z, A])(f: A => B): Either[Z, B] = fa.map(f)
  }

  object ListInstance extends Applicative[List] with Traversable[List] with Indexed[List] {
    final def pure[A](a: A): List[A] = List(a)
    final def map2[A, B, C](fa: List[A], fb: List[B])(f: (A, B) => C): List[C] =
      fa.flatMap(a => fb.map(b => f(a, b)))
    final def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)
    final def traverse[G[_], A, B](fa: List[A])(f: A => G[B])(implicit G: Applicative[G]): G[List[B]] =
      fa.foldRight(G.pure(List.empty[B]))((a, acc) => G.map2(f(a), acc)(_ :: _))
    final def indexed[A](fa: List[A]): List[(Int, A)] = fa.zipWithIndex.map(_.swap)
  }

}
