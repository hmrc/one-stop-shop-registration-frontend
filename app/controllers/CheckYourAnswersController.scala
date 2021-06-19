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

import cats.data.Validated.{Invalid, Valid}
import com.google.inject.Inject
import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import models.NormalMode
import models.audit.{RegistrationAuditModel, SubmissionResult}
import models.responses.ConflictFound
import pages.CheckYourAnswersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AuditService, EmailService, RegistrationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.{PreviousRegistrationSummary, PreviouslyRegisteredSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful

class CheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  cc: AuthenticatedControllerComponents,
  registrationConnector: RegistrationConnector,
  registrationService: RegistrationService,
  auditService: AuditService,
  view: CheckYourAnswersView,
  emailService: EmailService
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          SellsGoodsFromNiSummary.row(request.userAnswers),
          InControlOfMovingGoodsSummary.row(request.userAnswers),
          RegisteredCompanyNameSummary.row(request.userAnswers),
          PartOfVatGroupSummary.row(request.userAnswers),
          UkVatEffectiveDateSummary.row(request.userAnswers),
          BusinessAddressInUkSummary.row(request.userAnswers),
          UkAddressSummary.row(request.userAnswers),
          InternationalAddressSummary.row(request.userAnswers),
          HasTradingNameSummary.row(request.userAnswers),
          TradingNameSummary.checkAnswersRow(request.userAnswers),
          TaxRegisteredInEuSummary.row(request.userAnswers),
          EuDetailsSummary.checkAnswersRow(request.userAnswers),
          CurrentlyRegisteredInEuSummary.row(request.userAnswers),
          CurrentCountryOfRegistrationSummary.row(request.userAnswers),
          CurrentlyRegisteredInCountrySummary.row(request.userAnswers),
          PreviouslyRegisteredSummary.row(request.userAnswers),
          PreviousRegistrationSummary.checkAnswersRow(request.userAnswers),
          HasWebsiteSummary.row(request.userAnswers),
          WebsiteSummary.checkAnswersRow(request.userAnswers),
          BusinessContactDetailsSummary.row(request.userAnswers),
          BankDetailsSummary.row(request.userAnswers)
        ).flatten
      )

      Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      val registration = registrationService.fromUserAnswers(request.userAnswers, request.vrn)

      registration match {
        case Valid(registration) =>
          registrationConnector.submitRegistration(registration).flatMap {
            case Right(_) =>
              auditService.audit(RegistrationAuditModel.build(registration, SubmissionResult.Success, request))
              emailService.sendConfirmationEmail(
                registration.contactDetails.fullName,
                registration.registeredCompanyName,
                request.vrn.toString(),
                registration.contactDetails.emailAddress
              ) flatMap {
                _ => successful(Redirect(CheckYourAnswersPage.navigate(NormalMode, request.userAnswers)))
              }
            case Left(ConflictFound) =>
              auditService.audit(RegistrationAuditModel.build(registration, SubmissionResult.Duplicate, request))
              successful(Redirect(routes.AlreadyRegisteredController.onPageLoad()))

            case Left(e) =>
              logger.error(s"Unexpected result on submit: ${e.toString}")
              auditService.audit(RegistrationAuditModel.build(registration, SubmissionResult.Failure, request))
              successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }

        case Invalid(errors) =>
          val errorMessages = errors.map(_.errorMessage).toChain.toList.mkString("\n")
          logger.error(s"Unable to create a registration request from user answers: $errorMessages")
          successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
