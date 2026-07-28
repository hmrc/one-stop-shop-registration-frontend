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
import controllers.amend.routes as amendRoutes
import logging.Logging
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.domain.{PreviousRegistration, Registration, VatCustomerInfo}
import models.exclusions.ExclusionReason.Reversal
import models.requests.{AuthenticatedDataRequest, AuthenticatedMandatoryDataRequest}
import models.{AmendMode, NormalMode}
import pages.amend.ChangeYourRegistrationPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import queries.OriginalRegistrationQuery
import services.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckExistingRegistrations.checkExistingRegistration
import utils.CompletionChecks
import utils.FutureSyntax.*
import viewmodels.checkAnswers.*
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.{PreviousRegistrationSummary, PreviouslyRegisteredSummary}
import viewmodels.govuk.summarylist.*
import views.html.amend.ChangeYourRegistrationView

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeYourRegistrationController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  cc: AuthenticatedControllerComponents,
                                                  registrationConnector: RegistrationConnector,
                                                  registrationValidationService: RegistrationValidationService,
                                                  auditService: AuditService,
                                                  registrationService: RegistrationService,
                                                  frontendAppConfig: FrontendAppConfig,
                                                  clock: Clock,
                                                  view: ChangeYourRegistrationView,
                                                  commencementDateSummary: CommencementDateSummary
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with CompletionChecks {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetDataWithOss(Some(AmendMode)).async {
    implicit request: AuthenticatedMandatoryDataRequest[AnyContent] =>

      val existingPreviousRegistrations = checkExistingRegistration()(request.request).previousRegistrations

      val vatRegistrationDetailsList = SummaryListViewModel(
        rows = Seq(
          VatRegistrationDetailsSummary.rowBusinessName(request.userAnswers),
          VatRegistrationDetailsSummary.rowPartOfVatUkGroup(request.userAnswers),
          VatRegistrationDetailsSummary.rowUkVatRegistrationDate(request.userAnswers),
          VatRegistrationDetailsSummary.rowBusinessAddress(request.userAnswers)
        ).flatten
      )

      commencementDateSummary.row(request.userAnswers)(request = request.request).flatMap { cds =>

        val isExcluded: Boolean = request.registration.excludedTrader.exists(_.exclusionReason != Reversal)

        val list: SummaryList = detailList(existingPreviousRegistrations, cds, isExcluded)(request.request)

        val isValid = validate()(request.request)

        val registrationDueForReview: Boolean = if (frontendAppConfig.registrationReviewEnabled) {
          request.registration.adminUse.changeDate.exists(changeDate => changeDateMoreThanTwoYears(changeDate))
        } else {
          false
        }

        request.userAnswers.vatInfo match {
          case Some(vatCustomerInfo: VatCustomerInfo) =>
            for {
              originalUserAnswers <- registrationService.toUserAnswers(request.userId, request.registration, vatCustomerInfo)
              userAnswersWithoutOriginalRegistration <- Future.fromTry(request.userAnswers.remove(OriginalRegistrationQuery))
            } yield {

              val noAmendments = originalUserAnswers.data == userAnswersWithoutOriginalRegistration.data

              val unusableStatus: Boolean = request.registration.unusableStatus.contains(true)
              val noAmendmentsWithUnusableStatusCheck: Boolean = noAmendments && !unusableStatus

              Ok(view(vatRegistrationDetailsList, list, isValid, noAmendmentsWithUnusableStatusCheck, registrationDueForReview, frontendAppConfig.ossYourAccountUrl, AmendMode))
            }
          case None =>
            val errorMessage: String = "Vat information was not found"
            logger.error(errorMessage)
            val exception: Exception = new Exception(errorMessage)
            throw exception
        }
      }
  }

  def onSubmit(incompletePrompt: Boolean): Action[AnyContent] = cc.authAndGetDataWithOss(Some(AmendMode)).async {
    implicit request: AuthenticatedMandatoryDataRequest[AnyContent] =>

      getFirstValidationErrorRedirect(AmendMode)(request.request) match {
        case Some(errorRedirect) => if (incompletePrompt) {
          errorRedirect.toFuture
        } else {
          Redirect(routes.ChangeYourRegistrationController.onPageLoad()).toFuture
        }
        case None =>
          registrationValidationService.fromUserAnswers(request.userAnswers, request.vrn)(request = request.request).flatMap {
            case Valid(registration) =>
              val registrationWithOriginalSubmissionReceived = registration.copy(submissionReceived = request.registration.submissionReceived)
              registrationConnector.amendRegistration(registrationWithOriginalSubmissionReceived).flatMap {
                case Right(_) =>
                  auditService.audit(
                    RegistrationAuditModel.build(
                      RegistrationAuditType.AmendRegistration,
                      registrationWithOriginalSubmissionReceived,
                      SubmissionResult.Success,
                      request.request
                    ))
                  Redirect(ChangeYourRegistrationPage.navigate(NormalMode, request.userAnswers)).toFuture

                case Left(e) =>
                  logger.error(s"Unexpected result on submit: ${e.toString}")
                  auditService.audit(RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Failure, request.request))
                  Redirect(amendRoutes.ErrorSubmittingAmendmentController.onPageLoad()).toFuture
              }

            case Invalid(errors) =>
              getFirstValidationErrorRedirect(AmendMode)(request.request).map(
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
  }

  private def detailList(
                          existingPreviousRegistrations: Seq[PreviousRegistration],
                          cds: Option[SummaryListRow],
                          isExcluded: Boolean
                        )(implicit request: AuthenticatedDataRequest[AnyContent]) = {

    SummaryListViewModel(
      rows = (getTradingNameRows(isExcluded) ++
        getSalesRows(cds, isExcluded) ++
        getPreviouslyRegisteredRows(existingPreviousRegistrations, isExcluded) ++
        getRegisteredInEuRows(isExcluded) ++
        Seq(IsOnlineMarketplaceSummary.row(request.userAnswers, AmendMode, isExcluded)) ++
        getWebsiteRows(isExcluded) ++
        getBusinessContactDetailsRows() ++
        getBankDetailsRows()
        ).flatten
    )
  }

  private def getTradingNameRows(isExcluded: Boolean)(implicit request: AuthenticatedDataRequest[_]) = {
    val tradingNameSummaryRow = TradingNameSummary.checkAnswersRow(request.userAnswers, AmendMode, isExcluded)
    Seq(new HasTradingNameSummary().row(request.userAnswers, AmendMode, isExcluded).map { sr =>
      if (tradingNameSummaryRow.isDefined) {
        sr.withCssClass("govuk-summary-list__row--no-border")
      } else {
        sr
      }
    },
      tradingNameSummaryRow)
  }

  private def getSalesRows(cds: Option[SummaryListRow], isExcluded: Boolean)(implicit request: AuthenticatedDataRequest[_]) = {
    Seq(
      HasMadeSalesSummary.row(request.userAnswers, AmendMode, isExcluded).map(_.withCssClass("govuk-summary-list__row--no-border")),
      DateOfFirstSaleSummary.row(request.userAnswers, AmendMode, isExcluded).map(_.withCssClass("govuk-summary-list__row--no-border")),
      cds
    )
  }

  private def getPreviouslyRegisteredRows(
                                           existingPreviousRegistrations: Seq[PreviousRegistration],
                                           isExcluded: Boolean
                                         )(implicit request: AuthenticatedDataRequest[_]) = {
    val previousRegistrationSummaryRow = PreviousRegistrationSummary.checkAnswersRow(request.userAnswers, existingPreviousRegistrations, AmendMode, isExcluded)
    Seq(
      PreviouslyRegisteredSummary.row(request.userAnswers, AmendMode, isExcluded).map { sr =>
        if (previousRegistrationSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      previousRegistrationSummaryRow
    )
  }

  private def getRegisteredInEuRows(isExcluded: Boolean)(implicit request: AuthenticatedDataRequest[_]) = {
    val euDetailsSummaryRow = EuDetailsSummary.checkAnswersRow(request.userAnswers, AmendMode, isExcluded)
    Seq(
      TaxRegisteredInEuSummary.row(request.userAnswers, AmendMode, isExcluded).map { sr =>
        if (euDetailsSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      euDetailsSummaryRow
    )
  }

  private def getWebsiteRows(isExcluded: Boolean)(implicit request: AuthenticatedDataRequest[_]) = {
    val websiteSummaryRow = WebsiteSummary.checkAnswersRow(request.userAnswers, AmendMode, isExcluded)
    Seq(websiteSummaryRow)
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
  
  private def changeDateMoreThanTwoYears(changeDate: LocalDateTime): Boolean = {
    val twoYearsAgo = LocalDateTime.now(clock).minusYears(2)

    changeDate.isBefore(twoYearsAgo)
  }
}
