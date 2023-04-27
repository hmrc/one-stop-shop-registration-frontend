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

import cats.data.Validated.{Invalid, Valid}
import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import models.{CheckMode, NormalMode}
import models.audit.{RegistrationAuditModel, SubmissionResult}
import models.domain.Registration
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.requests.AuthenticatedDataRequest
import models.responses.ConflictFound
import pages.CheckYourAnswersPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import queries.EmailConfirmationQuery
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CompletionChecks
import utils.FutureSyntax._
import viewmodels.checkAnswers._
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.{PreviouslyRegisteredSummary, PreviousRegistrationSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            cc: AuthenticatedControllerComponents,
                                            registrationConnector: RegistrationConnector,
                                            registrationService: RegistrationValidationService,
                                            auditService: AuditService,
                                            view: CheckYourAnswersView,
                                            emailService: EmailService,
                                            dateService: DateService,
                                            saveForLaterService: SaveForLaterService,
                                            frontendAppConfig: FrontendAppConfig
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with CompletionChecks {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetDataAndCheckVerifyEmail().async {
    implicit request =>
      val vatRegistrationDetailsList = SummaryListViewModel(
        rows = Seq(
          VatRegistrationDetailsSummary.rowBusinessName(request.userAnswers),
          VatRegistrationDetailsSummary.rowIndividualName(request.userAnswers),
          VatRegistrationDetailsSummary.rowPartOfVatUkGroup(request.userAnswers),
          VatRegistrationDetailsSummary.rowUkVatRegistrationDate(request.userAnswers),
          VatRegistrationDetailsSummary.rowBusinessAddress(request.userAnswers)
        ).flatten
      )

      new CommencementDateSummary(dateService).row(request.userAnswers).map { commencementDateSummary =>

        val list = SummaryListViewModel(
          rows = Seq(
            new HasTradingNameSummary().row(request.userAnswers, CheckMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
            TradingNameSummary.checkAnswersRow(request.userAnswers, CheckMode),
            HasMadeSalesSummary.row(request.userAnswers).map(_.withCssClass("govuk-summary-list__row--no-border")),
            IsPlanningFirstEligibleSaleSummary.row(request.userAnswers).map(_.withCssClass("govuk-summary-list__row--no-border")),
            DateOfFirstSaleSummary.row(request.userAnswers).map(_.withCssClass("govuk-summary-list__row--no-border")),
            Some(commencementDateSummary),
            PreviouslyRegisteredSummary.row(request.userAnswers).map(_.withCssClass("govuk-summary-list__row--no-border")),
            PreviousRegistrationSummary.checkAnswersRow(request.userAnswers),
            TaxRegisteredInEuSummary.row(request.userAnswers, CheckMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
            EuDetailsSummary.checkAnswersRow(request.userAnswers, CheckMode),
            IsOnlineMarketplaceSummary.row(request.userAnswers, CheckMode),
            HasWebsiteSummary.row(request.userAnswers, CheckMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
            WebsiteSummary.checkAnswersRow(request.userAnswers, CheckMode),
            BusinessContactDetailsSummary.rowContactName(request.userAnswers).map(_.withCssClass("govuk-summary-list__row--no-border")),
            BusinessContactDetailsSummary.rowTelephoneNumber(request.userAnswers).map(_.withCssClass("govuk-summary-list__row--no-border")),
            BusinessContactDetailsSummary.rowEmailAddress(request.userAnswers),
            BankDetailsSummary.rowAccountName(request.userAnswers, CheckMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
            BankDetailsSummary.rowBIC(request.userAnswers, CheckMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
            BankDetailsSummary.rowIBAN(request.userAnswers, CheckMode)
          ).flatten
        )

        val isValid = validate()
        Ok(view(vatRegistrationDetailsList, list, isValid))
      }
  }

  def onSubmit(incompletePrompt: Boolean): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      registrationService.fromUserAnswers(request.userAnswers, request.vrn).flatMap {
        case Valid(registration) =>
          registrationConnector.submitRegistration(registration).flatMap {
            case Right(_) =>
              auditService.audit(RegistrationAuditModel.build(registration, SubmissionResult.Success, request))
              sendEmailConfirmation(request, registration)
            case Left(ConflictFound) =>
              auditService.audit(RegistrationAuditModel.build(registration, SubmissionResult.Duplicate, request))
              Redirect(routes.AlreadyRegisteredController.onPageLoad()).toFuture

            case Left(e) =>
              logger.error(s"Unexpected result on submit: ${e.toString}")
              auditService.audit(RegistrationAuditModel.build(registration, SubmissionResult.Failure, request))
              saveForLaterService.saveAnswers(
                routes.ErrorSubmittingRegistrationController.onPageLoad(),
                routes.CheckYourAnswersController.onPageLoad()
              )
          }

        case Invalid(errors) =>
          getFirstValidationErrorRedirect().map(
            errorRedirect => if (incompletePrompt) {
              errorRedirect.toFuture
            } else {
              Redirect(routes.CheckYourAnswersController.onPageLoad()).toFuture
            }
          ).getOrElse {
            val errorList = errors.toChain.toList
            val errorMessages = errorList.map(_.errorMessage).mkString("\n")
            logger.error(s"Unable to create a registration request from user answers: $errorMessages")
            saveForLaterService.saveAnswers(
              routes.ErrorSubmittingRegistrationController.onPageLoad(),
              routes.CheckYourAnswersController.onPageLoad()
            )
          }
      }
  }

  private def sendEmailConfirmation(
                                     request: AuthenticatedDataRequest[AnyContent],
                                     registration: Registration
                                   )(implicit hc: HeaderCarrier, messages: Messages): Future[Result] = {
    if (frontendAppConfig.registrationEmailEnabled) {
      emailService.sendConfirmationEmail(
        registration.contactDetails.fullName,
        registration.registeredCompanyName,
        registration.commencementDate,
        registration.contactDetails.emailAddress
      ) flatMap {
        emailConfirmationResult =>
          val emailSent = EMAIL_ACCEPTED == emailConfirmationResult
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EmailConfirmationQuery, emailSent))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield {
            Redirect(CheckYourAnswersPage.navigate(NormalMode, request.userAnswers))
          }
      }
    } else {
      val emailSent = false
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(EmailConfirmationQuery, emailSent))
        _ <- cc.sessionRepository.set(updatedAnswers)
      } yield {
        Redirect(CheckYourAnswersPage.navigate(NormalMode, request.userAnswers))
      }
    }
  }

}
