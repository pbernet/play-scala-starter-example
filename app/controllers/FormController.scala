package controllers

import javax.inject._

import model.UserData
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, _}


@Singleton
class FormController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  def showPage = Action {implicit request =>
    Ok(views.html.form(FormController.userForm))
  }

  def submit = Action { implicit request =>

    val userData = FormController.userForm.bindFromRequest.get
println("UserData from Request: " + userData)
    Ok(views.html.form(FormController.userForm))
  }
}

object FormController {
  val userForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "age" -> number(min = 0, max = 100)
    )(UserData.apply)(UserData.unapply)
  )

}
