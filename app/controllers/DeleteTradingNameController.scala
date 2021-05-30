/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.DeleteTradingNameFormProvider
import models.requests.DataRequest
import models.{Index, Mode}
import navigation.Navigator
import pages.{DeleteTradingNamePage, TradingNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeleteTradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteTradingNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         navigator: Navigator,
                                         formProvider: DeleteTradingNameFormProvider,
                                         view: DeleteTradingNameView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getTradingName(index) {
        tradingName =>
          Future.successful(Ok(view(form, mode, index, tradingName)))
      }

  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getTradingName(index) {
        tradingName =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, index, tradingName))),

            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(TradingNamePage(index)))
                  _              <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(DeleteTradingNamePage(index), mode, updatedAnswers))
              } else {
                Future.successful(Redirect(navigator.nextPage(DeleteTradingNamePage(index), mode, request.userAnswers)))
              }
          )
      }
  }

  private def getTradingName(index: Index)
                            (block: String => Future[Result])
                            (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(TradingNamePage(index)).map {
      name =>
        block(name)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
