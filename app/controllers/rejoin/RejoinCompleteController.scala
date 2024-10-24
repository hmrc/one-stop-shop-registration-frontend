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

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions._
import formats.Format.dateFormatter
import logging.Logging
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DateService, PeriodService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rejoin.RejoinCompleteView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RejoinCompleteController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          cc: AuthenticatedControllerComponents,
                                          view: RejoinCompleteView,
                                          frontendAppConfig: FrontendAppConfig,
                                          registrationConnector: RegistrationConnector,
                                          dateService: DateService,
                                          periodService: PeriodService
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData andThen cc.requireData(None)).async {
    implicit request =>
      for {
        externalEntryUrl <- registrationConnector.getSavedExternalEntry()
        maybeCalculatedCommencementDate <- dateService.calculateCommencementDate(request.userAnswers)
        calculatedCommencementDate = maybeCalculatedCommencementDate.getOrElse {
          val exception = new IllegalStateException("A calculated commencement date is expected, otherwise it wasn't submitted")
          logger.error(exception.getMessage, exception)
          throw exception
        }
      } yield {
        val organisationName = getOrganisationName(request.userAnswers)
        val savedUrl = externalEntryUrl.fold(_ => None, _.url)
        val periodOfFirstReturn = periodService.getFirstReturnPeriod(calculatedCommencementDate)
        val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
        val firstDayOfNextPeriod = nextPeriod.firstDay
        Ok(
          view(
            request.vrn.vrn,
            frontendAppConfig.feedbackUrl,
            savedUrl,
            yourAccountUrl = frontendAppConfig.ossYourAccountUrl,
            organisationName,
            calculatedCommencementDate.format(dateFormatter),
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter)
          )
        )
      }
  }

  private def getOrganisationName(answers: UserAnswers): String =
    answers.vatInfo.flatMap { vatInfo =>
      vatInfo.organisationName.fold(vatInfo.individualName)(Some.apply)
    }.getOrElse(throw new RuntimeException("OrganisationName has not been set in answers"))
}
