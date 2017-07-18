package controllers

import javax.inject._

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueue}
import play.api.Logger
import play.api.libs.EventSource
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * Sample from:
  * http://loicdescotte.github.io/posts/play-akka-streams-queue
  *
  * @param cc
  * @param actorSystem
  * @param exec
  */
@Singleton
class PlayAkkaSourceQueueController @Inject() (cc: ControllerComponents, implicit val actorSystem: ActorSystem, implicit val exec: ExecutionContext) extends AbstractController(cc) {

  implicit val materializer = ActorMaterializer()

  val Tick = "tick"

  class TickActor(queue: SourceQueue[String]) extends Actor {
    def receive = {
      case Tick => queue.offer("tack")
    }
  }

  def queueAction = Action {

    val (queueSource, futureQueue) = peekMatValue(Source.queue[String](10, OverflowStrategy.fail))

    futureQueue.map { queue =>

      val tickActor = actorSystem.actorOf(Props(new TickActor(queue)))
      val tickSchedule =
        actorSystem.scheduler.schedule(0 milliseconds,
          1 second,
          tickActor,
          Tick)

      queue.watchCompletion().map{ done =>
        Logger.debug("Client disconnected")
        tickSchedule.cancel
        Logger.debug("Scheduler canceled")
      }
    }

    //EventSource.flow format the String messages into the Server Sent Events format
    Ok.chunked(
      queueSource.map{e =>
        Logger.debug("queue source element : " + e)
        e
      }.via(EventSource.flow)
    )
  }

  //T is the source type, here String
  //M is the materialization type, here a SourceQueue[String]
  def peekMatValue[T, M](src: Source[T, M]): (Source[T, M], Future[M]) = {
    val p = Promise[M]
    val s = src.mapMaterializedValue { m =>
      p.trySuccess(m)
      m
    }
    (s, p.future)
  }
}
