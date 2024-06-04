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

package controllers.amend

import cats.data.Validated.{Invalid, Valid}
import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import controllers.amend.{routes => amendRoutes}
import logging.Logging
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.domain.{PreviousRegistration, Registration}
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.requests.AuthenticatedDataRequest
import models.{AmendMode, NormalMode}
import pages.amend.ChangeYourRegistrationPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import queries.EmailConfirmationQuery
import services._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckExistingRegistrations.checkExistingRegistration
import utils.CompletionChecks
import utils.FutureSyntax._
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.{PreviouslyRegisteredSummary, PreviousRegistrationSummary}
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.amend.ChangeYourRegistrationView

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
                                                  frontendAppConfig: FrontendAppConfig
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

        val list: SummaryList = detailList(existingPreviousRegistrations, cds)

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

  private def detailList(existingPreviousRegistrations: Seq[PreviousRegistration], cds: SummaryListRow)
                        (implicit request: AuthenticatedDataRequest[AnyContent]) = {

    SummaryListViewModel(
      rows = (getTradingNameRows() ++
        getSalesRows(cds) ++
        getPreviouslyRegisteredRows(existingPreviousRegistrations) ++
        getRegisteredInEuRows() ++
        Seq(IsOnlineMarketplaceSummary.row(request.userAnswers, AmendMode)) ++
        getWebsiteRows() ++
        getBusinessContactDetailsRows() ++
        getBankDetailsRows()
        ).flatten
    )
  }

  private def getTradingNameRows()(implicit request: AuthenticatedDataRequest[_]) = {
    val tradingNameSummaryRow = TradingNameSummary.checkAnswersRow(request.userAnswers, AmendMode)
    Seq(new HasTradingNameSummary().row(request.userAnswers, AmendMode).map { sr =>
      if (tradingNameSummaryRow.isDefined) {
        sr.withCssClass("govuk-summary-list__row--no-border")
      } else {
        sr
      }
    },
      tradingNameSummaryRow)
  }

  private def getSalesRows(cds: SummaryListRow)(implicit request: AuthenticatedDataRequest[_]) = {
    Seq(
      HasMadeSalesSummary.row(request.userAnswers, AmendMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      DateOfFirstSaleSummary.row(request.userAnswers, AmendMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      Some(cds)
    )
  }

  private def getPreviouslyRegisteredRows(existingPreviousRegistrations: Seq[PreviousRegistration])(implicit request: AuthenticatedDataRequest[_]) = {
    val previousRegistrationSummaryRow = PreviousRegistrationSummary.checkAnswersRow(request.userAnswers, existingPreviousRegistrations, AmendMode)
    Seq(
      PreviouslyRegisteredSummary.row(request.userAnswers, AmendMode).map { sr =>
        if (previousRegistrationSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      previousRegistrationSummaryRow
    )
  }

  private def getRegisteredInEuRows()(implicit request: AuthenticatedDataRequest[_]) = {
    val euDetailsSummaryRow = EuDetailsSummary.checkAnswersRow(request.userAnswers, AmendMode)
    Seq(
      TaxRegisteredInEuSummary.row(request.userAnswers, AmendMode).map { sr =>
        if (euDetailsSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      euDetailsSummaryRow
    )
  }

  private def getWebsiteRows()(implicit request: AuthenticatedDataRequest[_]) = {
    val websiteSummaryRow = WebsiteSummary.checkAnswersRow(request.userAnswers, AmendMode)
    Seq(
      HasWebsiteSummary.row(request.userAnswers, AmendMode).map { sr =>
        if (websiteSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      websiteSummaryRow
    )
  }

  private def getBusinessContactDetailsRows()(implicit request: AuthenticatedDataRequest[_]) = {
    Seq(
      BusinessContactDetailsSummary.rowContactName(request.userAnswers, AmendMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BusinessContactDetailsSummary.rowTelephoneNumber(request.userAnswers, AmendMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BusinessContactDetailsSummary.rowEmailAddress(request.userAnswers, AmendMode)
    )
  }

  private def getBankDetailsRows()(implicit request: AuthenticatedDataRequest[_]) = {
    Seq(
      BankDetailsSummary.rowAccountName(request.userAnswers, AmendMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BankDetailsSummary.rowBIC(request.userAnswers, AmendMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BankDetailsSummary.rowIBAN(request.userAnswers, AmendMode)
    )
  }

}
