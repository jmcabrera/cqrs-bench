package io.cqrs.bench

import _root_.akka.util.Timeout
import concurrent.duration._

package object akka {

  val atMost = 50 seconds
  implicit val timeout: Timeout = atMost

}