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

package controllers

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions._
import formats.Format.dateFormatter
import logging.Logging
import models.UserAnswers
import pages.BusinessContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import queries.EmailConfirmationQuery
import services.{DateService, PeriodService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationCompleteView
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ApplicationCompleteController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               cc: AuthenticatedControllerComponents,
                                               view: ApplicationCompleteView,
                                               frontendAppConfig: FrontendAppConfig,
                                               dateService: DateService,
                                               periodService: PeriodService,
                                               registrationConnector: RegistrationConnector
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData andThen cc.requireData(None)).async {
    implicit request => {

      for {
        externalEntryUrl <- registrationConnector.getSavedExternalEntry()
        maybeCalculatedCommencementDate <- dateService.calculateCommencementDate(request.userAnswers)
      } yield {
        val calculatedCommencementDate = maybeCalculatedCommencementDate.getOrElse {
          val exception = new IllegalStateException("A calculated commencement date is expected, otherwise it wasn't submitted")
          logger.error(exception.getMessage, exception)
          throw exception
        }
        (for {
          organisationName <- getOrganisationName(request.userAnswers)
          contactDetails <- request.userAnswers.get(BusinessContactDetailsPage)
          showEmailConfirmation <- request.userAnswers.get(EmailConfirmationQuery)
        } yield {
          val savedUrl = externalEntryUrl.fold(_ => None, _.url)
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(calculatedCommencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay
            Ok(
              view(
                HtmlFormat.escape(contactDetails.emailAddress).toString,
                request.vrn,
                showEmailConfirmation,
                frontendAppConfig.feedbackUrl,
                calculatedCommencementDate.format(dateFormatter),
                savedUrl,
                organisationName,
                periodOfFirstReturn.displayShortText,
                firstDayOfNextPeriod.format(dateFormatter)
              )
            )
        }).getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
  }

  private def getOrganisationName(answers: UserAnswers): Option[String] =
    answers.vatInfo match {
      case Some(vatInfo) if (vatInfo.organisationName.isDefined) => vatInfo.organisationName
      case Some(vatInfo) if (vatInfo.individualName.isDefined) => vatInfo.individualName
      case _ => None
    }
}
