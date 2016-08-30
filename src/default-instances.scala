/* -
 * Case Classy [case-classy]
 */

package classy

import scala.concurrent.duration._
import cats.data._

trait ClassyDefaultInstances {
  import DecodeError._

  implicit def yyzReadFiniteDuration[C: ReadValue[?, Duration]]: ReadValue[C, FiniteDuration] =
    ReadValue((source, key) ⇒
      ReadValue[C, Duration].apply(source, key) andThen {
        case fd: FiniteDuration ⇒ Validated.valid(fd)
        case d ⇒ Validated.invalidNel(
          BadFormat(key, s"$key is not a finite duration"))
      }
    )

  implicit def yyzReadDuration[C: ReadValue[?, String]]: ReadValue[C, Duration] =
    ReadValue((source, key) ⇒
      ReadValue[C, String].apply(source, key) andThen { value ⇒
        Xor.catchNonFatal(Duration(value))
          .leftMap(e ⇒ BadFormat(key, e.getMessage))
          .toValidatedNel
      }
    )
}
