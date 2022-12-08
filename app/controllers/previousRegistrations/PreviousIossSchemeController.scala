/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.previousRegistrations.PreviousIossSchemeFormProvider
import models.{Index, Mode, PreviousScheme}
import pages.previousRegistrations.{PreviousIossSchemePage, PreviousSchemePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.previousRegistrations.PreviousIossSchemeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousIossSchemeController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       formProvider: PreviousIossSchemeFormProvider,
                                       view: PreviousIossSchemeView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(PreviousIossSchemePage(countryIndex, schemeIndex)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, countryIndex, schemeIndex))
  }

  def onSubmit(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, countryIndex, schemeIndex))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviousIossSchemePage(countryIndex, schemeIndex), value))
            updatedAnswersWithPreviousScheme <- Future.fromTry(updatedAnswers.set(
              PreviousSchemePage(countryIndex, schemeIndex),
              if (value) {
                PreviousScheme.IOSSWI
              } else {
                PreviousScheme.IOSSWOI
              }
            ))
            _ <- cc.sessionRepository.set(updatedAnswersWithPreviousScheme)
          } yield Redirect(PreviousIossSchemePage(countryIndex, schemeIndex).navigate(mode, updatedAnswersWithPreviousScheme))
        }
      )
  }
}
