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
import forms.EuVatNumberFormProvider
import models.requests.DataRequest

import javax.inject.Inject
import models.{Country, Index, Mode}
import navigation.Navigator
import pages.{EuVatNumberPage, RegisteredCompanyNamePage, VatRegisteredEuMemberStatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EuVatNumberView

import scala.concurrent.{ExecutionContext, Future}

class EuVatNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: EuVatNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: EuVatNumberView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getCountry(index) {
        country =>

          val form = formProvider(country)

          val preparedForm = request.userAnswers.get(EuVatNumberPage(index)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, index, country)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getCountry(index) {
        country =>

          val form = formProvider(country)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, index, country))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(EuVatNumberPage(index), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(EuVatNumberPage(index), mode, updatedAnswers))
          )
      }
  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(VatRegisteredEuMemberStatePage(index)).map {
      country =>
        block(country)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
