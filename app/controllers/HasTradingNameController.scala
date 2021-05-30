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
import forms.HasTradingNameFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.{HasTradingNamePage, RegisteredCompanyNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.HasTradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasTradingNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         navigator: Navigator,
                                         formProvider: HasTradingNameFormProvider,
                                         view: HasTradingNameView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getRegisteredCompanyName {
        registeredCompanyName =>

          val form = formProvider(registeredCompanyName)

          val preparedForm = request.userAnswers.get(HasTradingNamePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, registeredCompanyName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getRegisteredCompanyName {
        registeredCompanyName =>

          val form = formProvider(registeredCompanyName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, registeredCompanyName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(HasTradingNamePage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(HasTradingNamePage, mode, updatedAnswers))
        )
      }
  }

  private def getRegisteredCompanyName(block: String => Future[Result])
                                      (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(RegisteredCompanyNamePage).map {
      name =>
        block(name)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
