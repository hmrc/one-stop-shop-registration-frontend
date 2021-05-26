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

import com.google.inject.Inject
import connectors.RegistrationConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.NormalMode
import models.responses.ConflictFound
import navigation.Navigator
import pages.CheckYourAnswersPage
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful

class CheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  registrationConnector: RegistrationConnector,
  registrationService: RegistrationService,
  navigator: Navigator,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          RegisteredCompanyNameSummary.row(request.userAnswers),
          HasTradingNameSummary.row(request.userAnswers),
          TradingNameSummary.checkAnswersRow(request.userAnswers),
          PartOfVatGroupSummary.row(request.userAnswers),
          UkVatNumberSummary.row(request.userAnswers),
          UkVatEffectiveDateSummary.row(request.userAnswers),
          UkVatRegisteredPostcodeSummary.row(request.userAnswers),
          VatRegisteredInEuSummary.row(request.userAnswers),
          EuVatDetailsSummary.checkAnswersRow(request.userAnswers),
          StartDateSummary.row(request.userAnswers),
          BusinessAddressSummary.row(request.userAnswers),
          WebsiteSummary.checkAnswersRow(request.userAnswers),
          BusinessContactDetailsSummary.row(request.userAnswers)
        ).flatten
      )

      Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val registrationRequest = registrationService.fromUserAnswers(request.userAnswers)

      registrationRequest match {
        case Some(registration) =>
          registrationConnector.submitRegistration(registration).flatMap {
            case Right(_) =>
              successful(Redirect(navigator.nextPage(CheckYourAnswersPage, NormalMode, request.userAnswers)))

            case Left(ConflictFound) =>
              successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))

            case Left(e) =>
              logger.error(s"Unexpected result on submit ${e.toString}")
              successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
        case None =>
          logger.error("Unable to create a registration request from user answers")
          successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
