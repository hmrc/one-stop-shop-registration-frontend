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

package controllers.amend

import cats.data.Validated.{Invalid, Valid}
import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import controllers.amend.{routes => amendRoutes}
import controllers.routes
import logging.Logging
import models.{AmendMode, NormalMode, UserAnswers}
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.domain.Registration
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.requests.AuthenticatedDataRequest
import pages.amend.ChangeYourRegistrationPage
import pages.DateOfFirstSalePage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import queries.EmailConfirmationQuery
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckExistingRegistrations.checkExistingRegistration
import utils.CompletionChecks
import utils.FutureSyntax._
import viewmodels.checkAnswers.{CommencementDateSummary, _}
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.{PreviouslyRegisteredSummary, PreviousRegistrationSummary}
import viewmodels.govuk.summarylist._
import views.html.amend.ChangeYourRegistrationView

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeYourRegistrationController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  cc: AuthenticatedControllerComponents,
                                                  registrationConnector: RegistrationConnector,
                                                  registrationService: RegistrationValidationService,
                                                  auditService: AuditService,
                                                  view: ChangeYourRegistrationView,
                                                  emailService: EmailService,
                                                  commencementDateSummary: CommencementDateSummary,
                                                  frontendAppConfig: FrontendAppConfig,
                                                  clock: Clock
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with CompletionChecks {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetData(Some(AmendMode)).async {
    implicit request =>

      val existingPreviousRegistrations = checkExistingRegistration().previousRegistrations

      val vatRegistrationDetailsList = SummaryListViewModel(
        rows = Seq(
          VatRegistrationDetailsSummary.rowBusinessName(request.userAnswers),
          VatRegistrationDetailsSummary.rowPartOfVatUkGroup(request.userAnswers),
          VatRegistrationDetailsSummary.rowUkVatRegistrationDate(request.userAnswers),
          VatRegistrationDetailsSummary.rowBusinessAddress(request.userAnswers)
        ).flatten
      )

        commencementDateSummary.row(request.userAnswers).map { cds =>

        val hasTradingNameSummaryRow = new HasTradingNameSummary().row(request.userAnswers, AmendMode)
        val tradingNameSummaryRow = TradingNameSummary.checkAnswersRow(request.userAnswers, AmendMode)
        val hasMadeSalesSummaryRow = HasMadeSalesSummary.row(request.userAnswers, AmendMode)
        val isPlanningFirstEligibleSaleSummaryRow = IsPlanningFirstEligibleSaleSummary.row(request.userAnswers, AmendMode)
        val dateOfFirstSaleSummaryRow = DateOfFirstSaleSummary.row(request.userAnswers, AmendMode)
        val commencementDateSummaryRow = Some(cds)
        val previouslyRegisteredSummaryRow = PreviouslyRegisteredSummary.row(request.userAnswers, AmendMode)
        val previousRegistrationSummaryRow = PreviousRegistrationSummary.checkAnswersRow(request.userAnswers, existingPreviousRegistrations, AmendMode)
        val taxRegisteredInEuSummaryRow = TaxRegisteredInEuSummary.row(request.userAnswers, AmendMode)
        val euDetailsSummaryRow = EuDetailsSummary.checkAnswersRow(request.userAnswers, AmendMode)
        val isOnlineMarketplaceSummaryRow = IsOnlineMarketplaceSummary.row(request.userAnswers, AmendMode)
        val hasWebsiteSummaryRow = HasWebsiteSummary.row(request.userAnswers, AmendMode)
        val websiteSummaryRow = WebsiteSummary.checkAnswersRow(request.userAnswers, AmendMode)
        val businessContactDetailsContactNameSummaryRow = BusinessContactDetailsSummary.rowContactName(request.userAnswers, AmendMode)
        val businessContactDetailsTelephoneSummaryRow = BusinessContactDetailsSummary.rowTelephoneNumber(request.userAnswers, AmendMode)
        val businessContactDetailsEmailSummaryRow = BusinessContactDetailsSummary.rowEmailAddress(request.userAnswers, AmendMode)
        val bankDetailsAccountNameSummaryRow = BankDetailsSummary.rowAccountName(request.userAnswers, AmendMode)
        val bankDetailsBicSummaryRow = BankDetailsSummary.rowBIC(request.userAnswers, AmendMode)
        val bankDetailsIbanSummaryRow = BankDetailsSummary.rowIBAN(request.userAnswers, AmendMode)

        val list = SummaryListViewModel(
          rows = Seq(
            hasTradingNameSummaryRow.map(sr =>
              if(tradingNameSummaryRow.isDefined) {
                sr.withCssClass("govuk-summary-list__row--no-border")
              } else {
                sr
              }),
            tradingNameSummaryRow,
            hasMadeSalesSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")), //TODO - this one?
            isPlanningFirstEligibleSaleSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")), //TODO - this one?
            dateOfFirstSaleSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")), //TODO - this one?
            commencementDateSummaryRow,
            previouslyRegisteredSummaryRow.map(sr =>
            if(previousRegistrationSummaryRow.isDefined) {
              sr.withCssClass("govuk-summary-list__row--no-border") //TODO - Check UA with no prevReg's
            } else {
              sr
            }),
            previousRegistrationSummaryRow,
            taxRegisteredInEuSummaryRow.map(sr =>
            if(euDetailsSummaryRow.isDefined) {
              sr.withCssClass("govuk-summary-list__row--no-border")
            } else {
              sr
            }),
            euDetailsSummaryRow,
            isOnlineMarketplaceSummaryRow,
            hasWebsiteSummaryRow.map(sr =>
            if(websiteSummaryRow.isDefined) {
              sr.withCssClass("govuk-summary-list__row--no-border")
            } else {
              sr
            }),
            websiteSummaryRow,
            businessContactDetailsContactNameSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
            businessContactDetailsTelephoneSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
            businessContactDetailsEmailSummaryRow,
            bankDetailsAccountNameSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
            bankDetailsBicSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
            bankDetailsIbanSummaryRow
          ).flatten
        )

        val isValid = validate()
        Ok(view(vatRegistrationDetailsList, list, isValid, AmendMode))
      }
  }

  def onSubmit(incompletePrompt: Boolean): Action[AnyContent] = cc.authAndGetData(Some(AmendMode)).async {
    implicit request =>
      registrationService.fromUserAnswers(request.userAnswers, request.vrn).flatMap {
        case Valid(registration) =>
          val registrationWithOriginalSubmissionReceived = registration.copy(submissionReceived = request.registration.flatMap(_.submissionReceived))
          registrationConnector.amendRegistration(registrationWithOriginalSubmissionReceived).flatMap {
            case Right(_) =>
              auditService.audit(
                RegistrationAuditModel.build(
                  RegistrationAuditType.AmendRegistration,
                  registrationWithOriginalSubmissionReceived,
                  SubmissionResult.Success,
                  request
                ))
              sendEmailConfirmation(request, registrationWithOriginalSubmissionReceived)

            case Left(e) =>
              logger.error(s"Unexpected result on submit: ${e.toString}")
              auditService.audit(RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Failure, request))
              Redirect(amendRoutes.ErrorSubmittingAmendmentController.onPageLoad()).toFuture
          }

        case Invalid(errors) =>
          getFirstValidationErrorRedirect(AmendMode).map(
            errorRedirect => if (incompletePrompt) {
              errorRedirect.toFuture
            } else {
              Redirect(amendRoutes.ChangeYourRegistrationController.onPageLoad()).toFuture
            }
          ).getOrElse {
            val errorList = errors.toChain.toList
            val errorMessages = errorList.map(_.errorMessage).mkString("\n")
            logger.error(s"Unable to create a registration request from user answers: $errorMessages")
            Redirect(amendRoutes.ErrorSubmittingAmendmentController.onPageLoad()).toFuture
          }
      }
  }

  private def sendEmailConfirmation(
                                     request: AuthenticatedDataRequest[AnyContent],
                                     registration: Registration
                                   )(implicit hc: HeaderCarrier, messages: Messages): Future[Result] = {
    if (frontendAppConfig.amendmentEmailEnabled) {
      emailService.sendConfirmationEmail(
        registration.contactDetails.fullName,
        registration.registeredCompanyName,
        registration.commencementDate,
        registration.contactDetails.emailAddress,
        AmendMode
      ) flatMap {
        emailConfirmationResult =>
          val emailSent = EMAIL_ACCEPTED == emailConfirmationResult
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EmailConfirmationQuery, emailSent))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield {
            Redirect(ChangeYourRegistrationPage.navigate(NormalMode, request.userAnswers))
          }
      }
    } else {
      val emailSent = false
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(EmailConfirmationQuery, emailSent))
        _ <- cc.sessionRepository.set(updatedAnswers)
      } yield {
        Redirect(ChangeYourRegistrationPage.navigate(NormalMode, request.userAnswers))
      }
    }
  }

}
