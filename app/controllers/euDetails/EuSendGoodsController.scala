package controllers.euDetails

import controllers.actions._
import forms.euDetails.EuSendGoodsFormProvider
import models.Mode
import pages.EuSendGoodsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EuSendGoodsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: EuSendGoodsFormProvider,
                                         view: EuSendGoodsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(EuSendGoodsPage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EuSendGoodsPage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(EuSendGoodsPage.navigate(mode, updatedAnswers))
      )
  }
}
