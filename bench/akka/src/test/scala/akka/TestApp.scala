package akka

import _root_.io.cqrs.bench.akka.AkkaCardService
import _root_.io.cqrs.bench.api.{ CreateCard, DoAuthorization }

object TestApp extends App {

  val cs = new AkkaCardService()

  cs.start
  for (i <- 1 to 10) {
    cs.handle(new CreateCard(""+i, "b"))
    cs.handle(new DoAuthorization(""+i, "b", 1))
  }
  cs.stop

}