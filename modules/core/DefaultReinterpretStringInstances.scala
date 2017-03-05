/* -
 * Case Classy [classy-core]
 */

package classy
package core

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import java.util.UUID

import Read.Reinterpret
import Read.Reinterpret.{ instance => reinterpret }

private[core] trait DefaultReinterpretStringInstances {

  implicit val reinterpretStringToBoolean: Reinterpret[String, Boolean] =
    reinterpret(defaultDecoders.decodeStringToBoolean)

  implicit val reinterpretStringToByte: Reinterpret[String, Byte] =
    reinterpret(defaultDecoders.decodeStringToByte)

  implicit val reinterpretStringToShort: Reinterpret[String, Short] =
    reinterpret(defaultDecoders.decodeStringToShort)

  implicit val reinterpretStringToInt: Reinterpret[String, Int] =
    reinterpret(defaultDecoders.decodeStringToInt)

  implicit val reinterpretStringToLong: Reinterpret[String, Long] =
    reinterpret(defaultDecoders.decodeStringToLong)

  implicit val reinterpretStringToFloat: Reinterpret[String, Float] =
    reinterpret(defaultDecoders.decodeStringToFloat)

  implicit val reinterpretStringToDouble: Reinterpret[String, Double] =
    reinterpret(defaultDecoders.decodeStringToDouble)

  implicit val reinterpretStringToUUID: Reinterpret[String, UUID] =
    reinterpret(defaultDecoders.decodeStringToUUID)

  implicit val reinterpretStringToDuration: Reinterpret[String, Duration] =
    reinterpret(defaultDecoders.decodeStringToDuration)

  implicit val reinterpretStringToFiniteDuration: Reinterpret[String, FiniteDuration] =
    reinterpret(defaultDecoders.decodeStringToFiniteDuration)

}
