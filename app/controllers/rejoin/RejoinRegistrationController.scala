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

package controllers.rejoin

import cats.data.Validated.{Invalid, Valid}
import connectors.RegistrationConnector
import controllers.actions._
import logging.Logging
import models.domain.{PreviousRegistration, PreviousRegistrationNew}
import models.RejoinMode
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.previousRegistrations.PreviousRegistrationDetails
import models.requests.AuthenticatedDataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.previousRegistration.AllPreviousRegistrationsQuery
import services.{AuditService, RegistrationValidationService, RejoinRegistrationService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CompletionChecks
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.{PreviousRegistrationSummary, PreviouslyRegisteredSummary}
import viewmodels.checkAnswers._
import viewmodels.govuk.all.{FluentSummaryListRow, SummaryListViewModel}
import views.html.rejoin.RejoinRegistrationView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RejoinRegistrationController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              cc: AuthenticatedControllerComponents,
                                              registrationConnector: RegistrationConnector,
                                              auditService: AuditService,
                                              registrationService: RegistrationValidationService,
                                              rejoinRegistrationService: RejoinRegistrationService,
                                              view: RejoinRegistrationView,
                                              commencementDateSummary: CommencementDateSummary,
                                              clock: Clock
                                            )
                                            (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with CompletionChecks {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetDataAndCheckVerifyEmail(Some(RejoinMode)).async {
    implicit request =>

      val date = LocalDate.now(clock)
      val maybeRegistration = request.registration match {
        case Some(reg) => Future.successful(reg)
        case None => registrationConnector.getRegistration().map(_.getOrElse(throw new IllegalStateException("Registration data is missing in the request")))
      }

      maybeRegistration.flatMap { registration =>
        val canRejoin = rejoinRegistrationService.canRejoinRegistration(date, registration.excludedTrader)

        if (canRejoin) {
          val userAnswers = request.userAnswers
          val vatRegistrationDetailsList = SummaryListViewModel(
            rows = Seq(
              VatRegistrationDetailsSummary.rowBusinessName(userAnswers),
              VatRegistrationDetailsSummary.rowPartOfVatUkGroup(userAnswers),
              VatRegistrationDetailsSummary.rowUkVatRegistrationDate(userAnswers),
              VatRegistrationDetailsSummary.rowBusinessAddress(userAnswers)
            ).flatten
          )

          commencementDateSummary.row(request.userAnswers).map { cds =>

            val list = detailList(cds)
            val isValid = validate()
            Ok(view(vatRegistrationDetailsList, list, isValid, RejoinMode))
          }
        } else {
          Redirect(controllers.rejoin.routes.CannotRejoinController.onPageLoad().url).toFuture
        }
      }

  }

  def onSubmit(incompletePrompt: Boolean): Action[AnyContent] = cc.authAndGetDataAndCheckVerifyEmail(Some(RejoinMode)).async {
    implicit request =>

      getFirstValidationErrorRedirect(RejoinMode).map { redirect =>
        Future.successful(redirect)
      }.getOrElse {
        val date = LocalDate.now(clock)
        val maybeRegistration = request.registration match {
          case Some(reg) => Future.successful(reg)
          case None => registrationConnector.getRegistration().map(_.getOrElse(throw new IllegalStateException("Registration data is missing in the request")))
        }

        maybeRegistration.flatMap { registration =>
          val canRejoin = rejoinRegistrationService.canRejoinRegistration(date, registration.excludedTrader)

          if (canRejoin) {
            registrationService.fromUserAnswers(request.userAnswers, request.vrn).flatMap {
              case Valid(registration) =>
                val rejoinRegistration = registration.copy(rejoin = Some(true))
              registrationConnector.amendRegistration(rejoinRegistration).flatMap {
                  case Right(_) =>
                    auditService.audit(
                      RegistrationAuditModel.build(
                        RegistrationAuditType.AmendRegistration,
                        registration,
                        SubmissionResult.Success,
                        request
                      ))
                    Redirect(routes.RejoinCompleteController.onPageLoad()).toFuture

                  case Left(e) =>
                    logger.error(s"Unexpected result on submit: ${e.toString}")
                    auditService.audit(RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Failure, request))
                    Redirect(routes.ErrorSubmittingRejoinController.onPageLoad()).toFuture
                }

              case Invalid(errors) =>
                getFirstValidationErrorRedirect(RejoinMode).map(
                  errorRedirect => if (incompletePrompt) {
                    errorRedirect.toFuture
                  } else {
                    Redirect(controllers.rejoin.routes.RejoinRegistrationController.onPageLoad()).toFuture
                  }
                ).getOrElse {
                  val errorList = errors.toChain.toList
                  val errorMessages = errorList.map(_.errorMessage).mkString("\n")
                  logger.error(s"Unable to create a registration request from user answers: $errorMessages")
                  Redirect(routes.ErrorSubmittingRejoinController.onPageLoad()).toFuture
                }
            }
          } else {
            Redirect(controllers.rejoin.routes.CannotRejoinController.onPageLoad().url).toFuture
          }
        }
      }
  }

  private def detailList(cds: SummaryListRow)(implicit request: AuthenticatedDataRequest[AnyContent]) = {

    SummaryListViewModel(
      rows =
        (
          getTradingNameRows() ++
            getSalesRows(cds) ++
            getPreviouslyRegisteredRows() ++
            getRegisteredInEuRows() ++
            Seq(IsOnlineMarketplaceSummary.row(request.userAnswers, RejoinMode)) ++
            getWebsiteRows() ++
            getBusinessContactDetailsRows() ++
            getBankDetailsRows()
          ).flatten
    )
  }

  private def getTradingNameRows()(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {
    val tradingNameSummaryRow = TradingNameSummary.checkAnswersRow(request.userAnswers, RejoinMode)
    Seq(new HasTradingNameSummary().row(request.userAnswers, RejoinMode).map { sr =>
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
      HasMadeSalesSummary.row(request.userAnswers, RejoinMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      DateOfFirstSaleSummary.row(request.userAnswers, RejoinMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      Some(cds)
    )
  }

  private def getPreviouslyRegisteredRows()
                                         (implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val previousRegistrationDetails: List[PreviousRegistrationDetails] =
      request.userAnswers.get(AllPreviousRegistrationsQuery).getOrElse(List.empty)
    val existingPreviousRegistrations: Seq[PreviousRegistration] = previousRegistrationDetails.map { details =>
      PreviousRegistrationNew(details.previousEuCountry, details.previousSchemesDetails)
    }

    val previousRegistrationSummaryRow = PreviousRegistrationSummary.checkAnswersRow(
      request.userAnswers,
      existingPreviousRegistrations,
      RejoinMode
    )

    Seq(
      PreviouslyRegisteredSummary.row(request.userAnswers, RejoinMode).map { sr =>
        if (previousRegistrationSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      previousRegistrationSummaryRow
    )
  }

  private def getRegisteredInEuRows()(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {
    val euDetailsSummaryRow = EuDetailsSummary.checkAnswersRow(request.userAnswers, RejoinMode)
    Seq(
      TaxRegisteredInEuSummary.row(request.userAnswers, RejoinMode).map { sr =>
        if (euDetailsSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      euDetailsSummaryRow
    )
  }

  private def getWebsiteRows()(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {
    val websiteSummaryRow = WebsiteSummary.checkAnswersRow(request.userAnswers, RejoinMode)
    Seq(
      HasWebsiteSummary.row(request.userAnswers, RejoinMode).map { sr =>
        if (websiteSummaryRow.isDefined) {
          sr.withCssClass("govuk-summary-list__row--no-border")
        } else {
          sr
        }
      },
      websiteSummaryRow
    )
  }

  private def getBusinessContactDetailsRows()(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {
    Seq(
      BusinessContactDetailsSummary.rowContactName(request.userAnswers, RejoinMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BusinessContactDetailsSummary.rowTelephoneNumber(request.userAnswers, RejoinMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BusinessContactDetailsSummary.rowEmailAddress(request.userAnswers, RejoinMode)
    )
  }

  private def getBankDetailsRows()(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {
    Seq(
      BankDetailsSummary.rowAccountName(request.userAnswers, RejoinMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BankDetailsSummary.rowBIC(request.userAnswers, RejoinMode).map(_.withCssClass("govuk-summary-list__row--no-border")),
      BankDetailsSummary.rowIBAN(request.userAnswers, RejoinMode)
    )
  }
}
