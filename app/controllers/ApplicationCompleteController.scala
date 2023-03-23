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

import config.FrontendAppConfig
import controllers.actions._
import formats.Format.dateFormatter
import models.UserAnswers
import models.core.{Match, MatchType}
import pages.DateOfFirstSalePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.external.ExternalReturnUrlQuery
import repositories.SessionRepository
import services.{CoreRegistrationValidationService, DateService, PeriodService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ExternalEntryUtils
import views.html.{ApplicationCompleteView, ApplicationCompleteWithEnrolmentView}

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ApplicationCompleteController @Inject()(
  override val messagesApi: MessagesApi,
  cc: AuthenticatedControllerComponents,
  view: ApplicationCompleteView,
  viewEnrolments: ApplicationCompleteWithEnrolmentView,
  frontendAppConfig: FrontendAppConfig,
  dateService: DateService,
  sessionRepository: SessionRepository,
  periodService: PeriodService,
  coreRegistrationValidationService: CoreRegistrationValidationService,
  clock: Clock
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData andThen cc.requireData).async {
    implicit request => {

      val id = ExternalEntryUtils.getSessionId()

      for {
        sessionData <- sessionRepository.get(id)
        maybeMatch <- coreRegistrationValidationService.searchUkVrn(request.vrn)

      } yield {
          {for {
            organisationName <- getOrganisationName(request.userAnswers)
            commencementDate <- getStartDate(request.userAnswers)

          } yield {
            val exclusionCommencementDate = getExclusionCommencementDate(maybeMatch, commencementDate)
            val savedUrl = sessionData.headOption.flatMap(_.get[String](ExternalReturnUrlQuery.path))
            val periodOfFirstReturn = periodService.getFirstReturnPeriod(exclusionCommencementDate)
            val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
            val firstDayOfNextPeriod = nextPeriod.firstDay
            if(frontendAppConfig.enrolmentsEnabled) {
              Ok(
                viewEnrolments(
                  request.vrn,
                  frontendAppConfig.feedbackUrl,
                  exclusionCommencementDate.format(dateFormatter),
                  savedUrl,
                  organisationName,
                  periodOfFirstReturn.displayShortText,
                  firstDayOfNextPeriod.format(dateFormatter)
                )
              )
            } else {
              Ok(
                view(
                  request.vrn,
                  frontendAppConfig.feedbackUrl,
                  exclusionCommencementDate.format(dateFormatter),
                  savedUrl,
                  organisationName,
                  periodOfFirstReturn.displayShortText,
                  firstDayOfNextPeriod.format(dateFormatter)
                )
              )
            }
          }}.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
  }

  private def isWithinLastDayOfRegistrationWhenTransferring(exclusionEffectiveDate: LocalDate): Boolean = {
    val lastDayOfRegistration = exclusionEffectiveDate.plusMonths(1).withDayOfMonth(10)
    LocalDate.now(clock).minusDays(1).isBefore(lastDayOfRegistration)
  }

  private def getStartDate(answers: UserAnswers): Option[LocalDate] =
    answers.get(DateOfFirstSalePage) match {
      case Some(startDate) => Some(dateService.startDateBasedOnFirstSale(startDate))
      case None            => Some(LocalDate.now())
    }

  private def getOrganisationName(answers: UserAnswers): Option[String] =
    answers.vatInfo match {
      case Some(vatInfo) => Some(vatInfo.organisationName)
      case _             => None
    }

  private def getExclusionCommencementDate(maybeMatch: Option[Match], commencementDate: LocalDate): LocalDate = {
    maybeMatch.filter(_.matchType == MatchType.TransferringMSID).flatMap { aMatch =>
      aMatch.exclusionEffectiveDate.map { exclusionEffectiveDate =>
        if (isWithinLastDayOfRegistrationWhenTransferring(exclusionEffectiveDate)) {
          exclusionEffectiveDate
        } else {
          commencementDate
        }
      }
    }.getOrElse(commencementDate)
  }
}
