/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package classy

import scala.Predef._

//import _root_.cats.instances.all._
import _root_.cats.kernel.laws._
import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.listOf
import org.scalacheck.Prop._

import org.scalacheck.derive._
import org.scalacheck.{ Shapeless => blackMagic }

import cats._

class DecodeErrorProperties extends Properties("DecodeError") {
  import DecodeError._

  val genLeaf: Gen[DecodeError] = {
    import blackMagic._
    MkArbitrary[LeafDecodeError].arbitrary.arbitrary
  }

  property("and two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => DecodeError.and(a, b) ?= And(a, b))

  property("&& two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => a && b ?= And(a, b))

  property("or two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => DecodeError.or(a, b) ?= Or(a, b))

  property("|| two leaf errors") =
    forAll(genLeaf, genLeaf)((a, b) => a || b ?= Or(a, b))

  property("and many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(DecodeError.and) ?= And(errors.head, errors.tail)))

  property("&& many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(_ && _) ?= And(errors.head, errors.tail)))

  property("or many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(DecodeError.or) ?= Or(errors.head, errors.tail)))

  property("|| many leaf errors") =
    forAll(listOf(genLeaf))(errors => errors.length >= 2 ==> (
      errors.reduce(_ || _) ?= Or(errors.head, errors.tail)))

  property("atPath") =
    forAll(
      arbitrary[String] :| "path",
      arbitrary[String] :| "missing path"
    )((path, missingPath) =>
        Missing.atPath(missingPath).atPath(path) ?=
          AtPath(path, AtPath(missingPath, Missing)))

  property("atIndex") =
    forAll(
      arbitrary[Int] :| "index",
      arbitrary[String] :| "missing path"
    )((index, missingPath) =>
        Missing.atPath(missingPath).atIndex(index) ?=
          AtIndex(index, AtPath(missingPath, Missing)))

  property("AtPath.deepError") =
    forAll(
      genLeaf :| "deep error",
      arbitrary[String] :| "paths head",
      arbitrary[List[String]] :| "paths tail"
    )((error, pathHead, pathTail) =>
        pathTail.foldLeft(
          error.atPath(pathHead)
        )(_ atPath _).deepError ?= error)

  {
    import blackMagic._
    implicit val arbitraryDecodeError = MkArbitrary[DecodeError].arbitrary

    {
      import DecodeErrorMonoid.and._
      include(GroupLaws[DecodeError].monoid.all, "and ")
    }

    {
      import DecodeErrorMonoid.or._
      include(GroupLaws[DecodeError].monoid.all, "or ")
    }
  }
}
