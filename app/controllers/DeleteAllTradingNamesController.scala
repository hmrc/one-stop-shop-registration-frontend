/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.DeleteAllTradingNamesFormProvider

import javax.inject.Inject
import models.Mode
import models.requests.AuthenticatedDataRequest
import pages.{DeleteAllTradingNamesPage, HasTradingNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.AllTradingNames
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeleteAllTradingNamesView

import scala.concurrent.{ExecutionContext, Future}

class DeleteAllTradingNamesController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: DeleteAllTradingNamesFormProvider,
                                         view: DeleteAllTradingNamesView
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

          determineRemoveAllTradingNamesAndRedirect(mode, value)
      )
  }

  private def determineRemoveAllTradingNamesAndRedirect(mode: Mode, value: Boolean)(implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    val removeTradingNames = if (value) {
      request.userAnswers.remove(AllTradingNames)
    } else {
      request.userAnswers.set(HasTradingNamePage, true)
    }
    for {
      updatedAnswers <- Future.fromTry(removeTradingNames)
      calculatedAnswers <- Future.fromTry(updatedAnswers.set(DeleteAllTradingNamesPage, value))
      _ <- cc.sessionRepository.set(calculatedAnswers)
    } yield Redirect(DeleteAllTradingNamesPage.navigate(mode, calculatedAnswers))
  }
}
