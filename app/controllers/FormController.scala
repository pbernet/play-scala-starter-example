package controllers

import javax.inject._

import model.UserData
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, _}


@Singleton
class FormController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  def form = Action {implicit request =>
    Ok(views.html.form(FormController.userForm))
  }

  def submit = Action { implicit request =>
    FormController.userForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.form(formWithErrors))
      },
      userData => {
        /* binding success, you get the actual value. */
        val newUser = model.UserData(userData.name, userData.age)
        val id = 1
        Ok(views.html.formconfirm(userData))
      }
    )
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
