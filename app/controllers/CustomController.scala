package controllers

import javax.inject._

import play.api.mvc._
import services.{Counter, CustomContent}


@Singleton
class CustomController @Inject()(cc: ControllerComponents,
                                 content: CustomContent) extends AbstractController(cc) {

  //def custom = Action { Ok(content.content.toString) }
  def custom = Action { Ok(content.content.toString) }

}
