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

package controllers.previousRegistrations

import controllers.actions._
import forms.previousRegistrations.PreviouslyRegisteredFormProvider
import models.Mode
import pages.previousRegistrations.PreviouslyRegisteredPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.previousRegistration.{AllPreviousRegistrationsRawQuery, DeriveNumberOfPreviousRegistrations}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckExistingRegistrations.cleanup
import views.html.previousRegistrations.PreviouslyRegisteredView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviouslyRegisteredController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: PreviouslyRegisteredFormProvider,
                                         view: PreviouslyRegisteredView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PreviouslyRegisteredPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviouslyRegisteredPage, value))
            finalAnswers <- Future.fromTry(cleanup(updatedAnswers, DeriveNumberOfPreviousRegistrations, AllPreviousRegistrationsRawQuery))
            _ <- cc.sessionRepository.set(finalAnswers)
          } yield {
            Redirect(PreviouslyRegisteredPage.navigate(mode, finalAnswers))
          }
      )
  }
}
