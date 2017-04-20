package classy

/** Provides pretty printing support for [[DecodeError]]
  *
  * This is currently marked private because the implementation is
  * subject to change. The current implementation is a "quick and
  * dirty" solution. Contributions to improve this code are welcomed!
  */
private[classy] object DecodeErrorPrinter {
  import DecodeError._

  def toPrettyString(error: DecodeError): String =
    stringify(error.atPath("errors"), 0, List.empty).map((prefix _).tupled).mkString("\n")

  private[this] def stringify(
    error: DecodeError,
    depth: Int,
    acc: List[(Int, String)]
  ): List[(Int, String)] = error match {

    case and: And =>
      val children = and.toList.flatMap(e =>
        stringify(e, depth, Nil))
      children ::: acc

    case or: Or =>
      val children = or.toList.flatMap(e =>
        stringify(e, depth, Nil))
      children ::: acc

    case atPath: AtPath =>
      val (pathStr, e) = collapse(atPath)
      val children = stringify(e, depth + 1, Nil)
      val msg = s"$pathStr${aggregateHeader(e)}"
      children match {
        case (_, childMsg) :: Nil =>
          (depth, s"$msg $childMsg") :: acc
        case _ =>
          (depth, msg) :: children ::: acc
      }

    case atIndex: AtIndex =>
      val (pathStr, e) = collapse(atIndex)
      val children = stringify(e, depth + 1, Nil)
      val msg = s"$pathStr${aggregateHeader(e)}"
      children match {
        case (_, childMsg) :: Nil =>
          (depth, s"$msg $childMsg") :: acc
        case _ =>
          (depth, msg) :: children ::: acc
      }

    case Missing =>
      (depth, "missing value") :: acc

    case WrongType(expected, Some(got)) =>
      (depth, s"expected $expected but got $got") :: acc

    case WrongType(expected, None) =>
      (depth, s"expected $expected") :: acc

    case Underlying(t) =>
      (depth, s"exception $t") :: acc

    case Truncated(raw, result) =>
      (depth, s"$raw was truncated to $result") :: acc

    case Identity =>
      acc
  }

  private[this] def aggregateHeader(error: DecodeError): String =
    error match {
      case or: Or   => " (disjunction/OR):"
      case and: And => " (adjunction/AND):"
      case _        => ":"
    }

  private[this] def collapse(
    atPath: AtPath
  ): (String, DecodeError) = {
    val (paths, e0) = collapseAtPath(atPath)
    val prefix0 = paths.reverse.mkString(".")
    e0 match {
      case atIndex: AtIndex =>
        val (prefix1, e1) = collapse(atIndex)
        (prefix0 + prefix1, e1)
      case _ =>
        (prefix0, e0)
    }
  }

  private[this] def collapse(
    atIndex: AtIndex
  ): (String, DecodeError) = {
    val (indicies, e0) = collapseAtIndex(atIndex)
    val prefix0 = "[" + indicies.reverse.mkString("][") + "]"
    e0 match {
      case atPath: AtPath =>
        val (prefix1, e1) = collapse(atPath)
        (prefix0 + prefix1, e1)
      case _ =>
        (prefix0, e0)
    }
  }

  private[this] def collapseAtPath(
    atPath: AtPath,
    paths: List[String] = Nil
  ): (List[String], DecodeError) = atPath.error match {
    case childPath: AtPath => collapseAtPath(childPath, atPath.path :: paths)
    case other             => (atPath.path :: paths, other)
  }

  private[this] def collapseAtIndex(
    atIndex: AtIndex,
    indicies: List[Int] = Nil
  ): (List[Int], DecodeError) = atIndex.error match {
    case childIndex: AtIndex => collapseAtIndex(childIndex, atIndex.index :: indicies)
    case other             => (atIndex.index :: indicies, other)
  }

  private[this] def prefix(depth: Int, string: String): String =
    if (depth <= 0) string
    else prefix(depth - 1, "  " + string)
}
