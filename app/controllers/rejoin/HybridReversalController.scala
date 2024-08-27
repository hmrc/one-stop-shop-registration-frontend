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
import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions._
import logging.Logging
import models.RejoinMode
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.domain.Registration
import models.exclusions.ExclusionReason.Reversal
import models.requests.AuthenticatedDataRequest
import pages.DateOfFirstSalePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AuditService, RegistrationValidationService, RejoinRegistrationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CompletionChecks
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.rejoin.HybridReversalView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HybridReversalController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          cc: AuthenticatedControllerComponents,
                                          registrationConnector: RegistrationConnector,
                                          auditService: AuditService,
                                          registrationService: RegistrationValidationService,
                                          rejoinRegistrationService: RejoinRegistrationService,
                                          view: HybridReversalView,
                                          appConfig: FrontendAppConfig,
                                          clock: Clock
                                        )
                                        (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with CompletionChecks {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetDataAndCheckVerifyEmail(Some(RejoinMode)).async {
    implicit request =>
      val date = LocalDate.now(clock)
      val registration = request.registration.getOrElse(throw new IllegalStateException("Registration data is missing in the request"))
      val canRejoin = rejoinRegistrationService.canRejoinRegistration(date, registration.excludedTrader)

      request.userAnswers.get(DateOfFirstSalePage)
        .map(value => rejoinRegistrationService.canReverse(value, request.registration.flatMap(_.excludedTrader))) match {
        case Some(true) if canRejoin =>
          val list = detailList()
          val isValid = validate()
          Ok(view(list, isValid, RejoinMode, appConfig.ossYourAccountUrl)).toFuture
        case _ => Redirect(controllers.rejoin.routes.CannotReverseController.onPageLoad().url).toFuture
      }

  }

  def onSubmit(incompletePrompt: Boolean): Action[AnyContent] = cc.authAndGetDataAndCheckVerifyEmail(Some(RejoinMode)).async {
    implicit request =>

      getFirstValidationErrorRedirect(RejoinMode).map { redirect =>
        Future.successful(redirect)
      }.getOrElse {
        val date = LocalDate.now(clock)
        val registration = request.registration.getOrElse(throw new IllegalStateException("Registration data is missing in the request"))

        val canRejoin = rejoinRegistrationService.canRejoinRegistration(date, registration.excludedTrader)

        if (canRejoin) {
          registrationService.fromUserAnswers(request.userAnswers, request.vrn).flatMap {
            case Valid(registration) =>
              amendAuditAndRedirect(registration)

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
          Redirect(controllers.rejoin.routes.CannotReverseController.onPageLoad().url).toFuture
        }
      }
  }

  private def amendAuditAndRedirect(registration: Registration)(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]) = {
    val reverseRegistration = registration.copy(
      exclusionDetails = registration.exclusionDetails.map(_.copy(exclusionReason = Reversal))
    )
    registrationConnector.amendRegistration(reverseRegistration).flatMap {
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
  }

  private def detailList()(implicit request: AuthenticatedDataRequest[AnyContent]) = {

    SummaryListViewModel(
      rows =
        getSalesRows().flatten
    )
  }

  private def getSalesRows()(implicit request: AuthenticatedDataRequest[_]) = {
    Seq(
      HasMadeSalesSummary.row(request.userAnswers, RejoinMode),
      DateOfFirstSaleSummary.row(request.userAnswers, RejoinMode)
    )
  }

}
