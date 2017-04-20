/* -
 * Case Classy [classy-core]
 */

package classy
package misc.wheel

import predef._

/** Covariant functor */
trait Functor[F[_]] extends Serializable {

  /** Applies function `f` to value `fa` */
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object Functor {
  def apply[F[_]](implicit ev: Functor[F]): Functor[F] = ev
}

/** Applicative functor */
trait Applicative[F[_]] extends Functor[F] {

  /** Lift a value into the applicative functor */
  def pure[A](a: A): F[A]

  /** Applies binary function `f` to values `fa` and `fb` */
  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]
}

object Applicative {
  def apply[F[_]](implicit ev: Applicative[F]): Applicative[F] = ev
  implicit val listApplicative: Applicative[List] = instances.ListInstance
}

/** Captures the ability to traverse a structure from left to right
  * while applying an effect
  */
trait Traversable[F[_]] extends Functor[F] {

  /** Thread an effect `f` through `fa` */
  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

  /** Thread effects `G` through `F`, returning `G` on the outside of `F` */
  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    traverse(fga)(ga => ga)
}

object Traversable {
  def apply[F[_]](implicit ev: Traversable[F]): Traversable[F] = ev
  implicit val listTraversable: Traversable[List] = instances.ListInstance
}

/** Captures the ability to assign indicies to a structure */
trait Indexed[F[_]] extends Serializable {

  /** Assign indicies to elements of `fa` */
  def indexed[A](fa: F[A]): F[(Int, A)]
}

object Indexed {
  implicit val listIndexed: Indexed[List] = instances.ListInstance
}

private[classy] object instances {

  final class EitherApplicative[Z] private[classy] (fz: (Z, Z) => Z) extends Applicative[Either[Z, ?]] {
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
