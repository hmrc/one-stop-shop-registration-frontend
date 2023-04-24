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
import forms.HasTradingNameFormProvider
import models.Mode
import models.requests.AuthenticatedDataRequest
import pages.HasTradingNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.HasTradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasTradingNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: HasTradingNameFormProvider,
                                         view: HasTradingNameView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCompanyName {
        companyName =>

          val form = formProvider()

          val preparedForm = request.userAnswers.get(HasTradingNamePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, companyName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCompanyName {
        companyName =>

          val form = formProvider()

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, companyName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(HasTradingNamePage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(HasTradingNamePage.navigate(mode, updatedAnswers))
        )
      }
  }

  private def getCompanyName(block: String => Future[Result])
                            (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.vatInfo match {
      case Some(vatInfo) =>
        val name = vatInfo.organisationName
        block(name)
      case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
