package io.cqrs.bench.akka

import concurrent.{ Await, Future }
import org.slf4j.LoggerFactory
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.routing.RoundRobinPool
import io.cqrs.bench.api.{ CardService, CreateCard, DoAuthorization }

class AkkaCardService extends CardService {

  implicit var sys: ActorSystem = _
  var man: ActorRef = _
  var es: ActorRef = _
  var eb: ActorRef = _
  var rep: ActorRef = _

  override def start() {
    sys = ActorSystem()

    // start event bus
    eb = sys.actorOf(Props[EventBus], "eb")

    // start event store
    es = sys.actorOf(Props[EventStore], "es")
    eb ! Register(es)

    rep = sys.actorOf(Props(classOf[Repository], eb), "rep")

    // start manager
    man = sys.actorOf(RoundRobinPool(5).props(Props(classOf[Manager], rep)), "manager")
  }

  override def handle(cc: CreateCard) =
    Await.result(man ? Api(cc), atMost).asInstanceOf[String]

  override def handle(da: DoAuthorization) =
    Await.result(man ? Api(da), atMost).asInstanceOf[String]

  override def clear() {
    // Send clear to manager and event store, and wait for both to reply
    implicit val ex = sys.dispatcher
    val f = man ? Clear
    val g = es ? Clear
    val h = Future.sequence(Seq(f, g))
    Await.result(h, atMost)
  }

  override def stop() {
    implicit val ex = sys.dispatcher
    Await.result(es ? Close, atMost)
    sys.shutdown
    sys.awaitTermination
  }

  override val parallel = true

  override val getName = "AKKA"

}

case object Clear
case object Close

object Api {
  def apply(cc: CreateCard) = CardCreation(cc.getPan(), cc.getEmbossedDate())
  def apply(da: DoAuthorization) = Authorization(da.getPan(), da.getEmbossedDate(), da.getAmount())
}
sealed trait Api
case class CardCreation(pan: String, embossedDate: String) extends Api
case class Authorization(pan: String, embossedDate: String, authorizedAmount: Long) extends Api

case class Manager(rep: ActorRef) extends Actor {
  import concurrent.duration._

  implicit val ex = context.dispatcher

  def receive = {
    case cc: CardCreation =>
      rep ! Create(cc.pan, cc.embossedDate)
      sender ! "00"
    case da: Authorization =>
      val s = sender()
      (rep ? Find(da.pan, da.embossedDate)) map {
        case None =>
          s ! "01"
        case Some(card: ActorRef) =>
          card ! (da, s)
        case _ =>
          s ! "02"
      }
    case Clear =>
      rep forward Clear
  }
}

sealed trait RepoAPI
case class Find(pan: String, embossedDate: String) extends RepoAPI
case class Create(pan: String, embossedDate: String) extends RepoAPI
case class Delete(pan: String, embossedDate: String) extends RepoAPI

case class Repository(eb: ActorRef) extends Actor {

  def receive = {
    case Find(pan, embossedDate) =>
      sender ! context.child(s"$pan-$embossedDate")
    case Create(pan, embossedDate) =>
      context.actorOf(Props(classOf[Card], pan, embossedDate, eb), s"$pan-$embossedDate")
      eb ! CardCreated(pan, embossedDate)
    case Delete(pan, embossedDate) =>
      context.child(s"$pan-$embossedDate") foreach {
        c =>
          context.stop(c)
          eb ! CardDeleted(pan, embossedDate)
      }
    case Clear =>
      context.children.map(context.stop(_))
      sender ! Clear
  }
}

case class Card(pan: String, embossedDate: String, eb: ActorRef) extends Actor {
  var amount: Long = 0

  def receive = {
    case (da: Authorization, s: ActorRef) =>
      amount = amount + da.authorizedAmount
      eb ! Authorized(pan, embossedDate, da.authorizedAmount)
      s ! "00"
  }
}

sealed trait EventBusAPI
case class Register(ref: ActorRef) extends EventBusAPI
case class Unregister(ref: ActorRef) extends EventBusAPI
case class EventBus extends Actor {
  val list = collection.mutable.Set.empty[ActorRef]
  def receive = {
    case Register(toAdd) =>
      list.add(toAdd)
    case Unregister(toDel) =>
      list.remove(toDel)
    case e: Event =>
      list.foreach {
        _ ! e
      }
  }
}

case class EventStore extends Actor {

  import java.io.{ PrintWriter, FileWriter, File, FileOutputStream }
  import java.util.zip.GZIPOutputStream

  var pw = new PrintWriter(new GZIPOutputStream(new FileOutputStream(new File("storage.txt"))), false)

  def receive = {
    case event: Event =>
      pw.println(event.toString())
    case Clear =>
      pw.close()
      pw = new PrintWriter(new FileWriter(new File("storage.txt")), false)
      sender ! Clear
    case Close =>
      pw.close()
      sender ! Close
  }

}

sealed trait Event
case class CardCreated(pan: String, embossedDate: String) extends Event
case class CardDeleted(pan: String, embossedDate: String) extends Event
case class Authorized(pan: String, embossedDate: String, authorizedAmount: Long) extends Event