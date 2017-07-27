package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * This controller creates an `Action` that demonstrates how to write
  * simple asynchronous code in a controller
  *
  * @param cc          standard controller components
  * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
  *                    run code after a delay.
  * @param exec        We need an `ExecutionContext` to execute our
  *                    asynchronous code.  When rendering content, you should use Play's
  *                    default execution context, which is dependency injected.  If you are
  *                    using blocking operations, such as database or network access, then you should
  *                    use a different custom execution context that has a thread pool configured for
  *                    a blocking API.
  */
@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  /**
    * Creates an Action that returns a composed text message
    * coming from two sources with a different delay
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/message`.
    */
  def message = Action.async {
    getFutureMessages(1.second).map { msg => Ok(msg) }
  }

  private def getFutureMessages(delayTime: FiniteDuration): Future[String] = {
    val promise1: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      promise1.success(" Hi! ")
    }(actorSystem.dispatcher)

    val promise2: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime * 2) {
      promise2.success(" Hi late! ")
    }(actorSystem.dispatcher)

    for {
      result1 <- promise1.future
      result2 <- promise2.future
    } yield result2 + result1
  }
}
