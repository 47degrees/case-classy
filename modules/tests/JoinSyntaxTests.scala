/* -
 * Case Classy [classy-tests]
 */

package classy
package core

import org.scalacheck._
import org.scalacheck.Prop._

class JoinSyntaxTests extends Properties("Decoder.join syntax") {

  type A = String
  type B = String
  type C = String
  type D = String
  type E = String
  type F = String
  type G = String
  type H = String
  type I = String
  type J = String
  type K = String
  type L = String
  type M = String
  type N = String
  type O = String
  type P = String
  type Q = String
  type R = String
  type S = String
  type T = String
  type U = String
  type V = String
  type W = String

  lazy val a: Decoder[Unit, A] = Decoder.const("A")
  lazy val b: Decoder[Unit, B] = Decoder.const("B")
  lazy val c: Decoder[Unit, C] = Decoder.const("C")
  lazy val d: Decoder[Unit, D] = Decoder.const("D")
  lazy val e: Decoder[Unit, E] = Decoder.const("E")
  lazy val f: Decoder[Unit, F] = Decoder.const("F")
  lazy val g: Decoder[Unit, G] = Decoder.const("G")
  lazy val h: Decoder[Unit, H] = Decoder.const("H")
  lazy val i: Decoder[Unit, I] = Decoder.const("I")
  lazy val j: Decoder[Unit, J] = Decoder.const("J")
  lazy val k: Decoder[Unit, K] = Decoder.const("K")
  lazy val l: Decoder[Unit, L] = Decoder.const("L")
  lazy val m: Decoder[Unit, M] = Decoder.const("M")
  lazy val n: Decoder[Unit, N] = Decoder.const("N")
  lazy val o: Decoder[Unit, O] = Decoder.const("O")
  lazy val p: Decoder[Unit, P] = Decoder.const("P")
  lazy val q: Decoder[Unit, Q] = Decoder.const("Q")
  lazy val r: Decoder[Unit, R] = Decoder.const("R")
  lazy val s: Decoder[Unit, S] = Decoder.const("S")
  lazy val t: Decoder[Unit, T] = Decoder.const("T")
  lazy val u: Decoder[Unit, U] = Decoder.const("U")
  lazy val v: Decoder[Unit, V] = Decoder.const("V")
  lazy val w: Decoder[Unit, W] = Decoder.const("W")

  lazy val j02: Decoder[Unit, (A, B)] =
    a join b
  lazy val j03: Decoder[Unit, (A, B, C)] =
    a join b join c
  lazy val j04: Decoder[Unit, (A, B, C, D)] =
    a join b join c join d
  lazy val j05: Decoder[Unit, (A, B, C, D, E)] =
    a join b join c join d join e
  lazy val j06: Decoder[Unit, (A, B, C, D, E, F)] =
    a join b join c join d join e join f
  lazy val j07: Decoder[Unit, (A, B, C, D, E, F, G)] =
    a join b join c join d join e join f join g
  lazy val j08: Decoder[Unit, (A, B, C, D, E, F, G, H)] =
    a join b join c join d join e join f join g join h
  lazy val j09: Decoder[Unit, (A, B, C, D, E, F, G, H, I)] =
    a join b join c join d join e join f join g join h join i
  lazy val j10: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J)] =
    a join b join c join d join e join f join g join h join i join j
  lazy val j11: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K)] =
    a join b join c join d join e join f join g join h join i join j join k
  lazy val j12: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L)] =
    a join b join c join d join e join f join g join h join i join j join k join l
  lazy val j13: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m
  lazy val j14: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n
  lazy val j15: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o
  lazy val j16: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o join p
  lazy val j17: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o join p join q
  lazy val j18: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o join p join q join r
  lazy val j19: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o join p join q join r join s
  lazy val j20: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o join p join q join r join s join t
  lazy val j21: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o join p join q join r join s join t join u
  lazy val j22: Decoder[Unit, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
    a join b join c join d join e join f join g join h join i join j join k join l join m join n join o join p join q join r join s join t join u join v

  property("join  2") = j02(()) ?= ("A", "B").right
  property("join  3") = j03(()) ?= ("A", "B", "C").right
  property("join  4") = j04(()) ?= ("A", "B", "C", "D").right
  property("join  5") = j05(()) ?= ("A", "B", "C", "D", "E").right
  property("join  6") = j06(()) ?= ("A", "B", "C", "D", "E", "F").right
  property("join  7") = j07(()) ?= ("A", "B", "C", "D", "E", "F", "G").right
  property("join  8") = j08(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H").right
  property("join  9") = j09(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I").right
  property("join 10") = j10(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J").right
  property("join 11") = j11(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K").right
  property("join 12") = j12(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L").right
  property("join 13") = j13(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M").right
  property("join 14") = j14(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N").right
  property("join 15") = j15(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O").right
  property("join 16") = j16(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P").right
  property("join 17") = j17(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q").right
  property("join 18") = j18(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R").right
  property("join 19") = j19(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S").right
  property("join 20") = j20(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T").right
  property("join 21") = j21(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U").right
  property("join 22") = j22(()) ?= ("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V").right
}
