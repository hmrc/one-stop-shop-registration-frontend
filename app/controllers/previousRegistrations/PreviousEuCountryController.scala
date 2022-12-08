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
import forms.previousRegistrations.PreviousEuCountryFormProvider
import models.{Index, Mode}
import pages.previousRegistrations.PreviousEuCountryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.previousRegistration.AllPreviousRegistrationsQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.previousRegistrations.PreviousEuCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousEuCountryController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: PreviousEuCountryFormProvider,
                                        view: PreviousEuCountryView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val form = formProvider(index, request.userAnswers.get(AllPreviousRegistrationsQuery).getOrElse(Seq.empty).map(_.previousEuCountry))

      val preparedForm = request.userAnswers.get(PreviousEuCountryPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      val form = formProvider(index, request.userAnswers.get(AllPreviousRegistrationsQuery).getOrElse(Seq.empty).map(_.previousEuCountry))

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, index))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviousEuCountryPage(index), value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(PreviousEuCountryPage(index).navigate(mode, updatedAnswers))
      )
  }
}
