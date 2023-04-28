/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.actions._
import forms.DeleteAllWebsitesFormProvider
import models.Mode
import models.requests.AuthenticatedDataRequest
import pages.{DeleteAllWebsitesPage, HasWebsitePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.AllWebsites
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeleteAllWebsitesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteAllWebsitesController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             cc: AuthenticatedControllerComponents,
                                             formProvider: DeleteAllWebsitesFormProvider,
                                             view: DeleteAllWebsitesView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)) {
    implicit request =>

      Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          determineRemoveAllWebsitesAndRedirect(mode, value)
      )
  }

  private def determineRemoveAllWebsitesAndRedirect(mode: Mode, value: Boolean)(implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    val removeWebsites = if (value) {
      request.userAnswers.remove(AllWebsites)
    } else {
      request.userAnswers.set(HasWebsitePage, true)
    }
    for {
      updatedAnswers <- Future.fromTry(removeWebsites)
      calculatedAnswers <- Future.fromTry(updatedAnswers.set(DeleteAllWebsitesPage, value))
      _ <- cc.sessionRepository.set(calculatedAnswers)
    } yield Redirect(DeleteAllWebsitesPage.navigate(mode, calculatedAnswers))
  }

}
