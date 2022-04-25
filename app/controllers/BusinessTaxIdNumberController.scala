package controllers

import controllers.actions._
import forms.BusinessTaxIdNumberFormProvider
import javax.inject.Inject
import models.Mode
import pages.BusinessTaxIdNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessTaxIdNumberView

import scala.concurrent.{ExecutionContext, Future}

class BusinessTaxIdNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: BusinessTaxIdNumberFormProvider,
                                        view: BusinessTaxIdNumberView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(BusinessTaxIdNumberPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessTaxIdNumberPage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(BusinessTaxIdNumberPage.navigate(mode, updatedAnswers))
      )
  }
}
