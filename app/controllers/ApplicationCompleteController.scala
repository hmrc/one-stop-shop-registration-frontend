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

import config.FrontendAppConfig
import controllers.actions._
import formats.Format.dateFormatter
import models.UserAnswers
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import queries.EmailConfirmationQuery
import services.DateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationCompleteView

import java.time.LocalDate
import javax.inject.Inject

class ApplicationCompleteController @Inject()(
  override val messagesApi: MessagesApi,
  cc: AuthenticatedControllerComponents,
  view: ApplicationCompleteView,
  frontendAppConfig: FrontendAppConfig,
  dateService: DateService
) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData andThen cc.requireData) {
    implicit request => {
      for {
        contactDetails        <- request.userAnswers.get(BusinessContactDetailsPage)
        showEmailConfirmation <- request.userAnswers.get(EmailConfirmationQuery)
        commencementDate      <- getStartDate(request.userAnswers)
      } yield {
        val dateOfFirstSale   = request.userAnswers.get(DateOfFirstSalePage)
        val vatReturnEndDate  = dateService.getVatReturnEndDate(commencementDate)
        val vatReturnDeadline = dateService.getVatReturnDeadline(vatReturnEndDate)
        val isDOFSDifferentToCommencementDate =
          dateService.isDOFSDifferentToCommencementDate(dateOfFirstSale, commencementDate)

        Ok(
          view(
            HtmlFormat.escape(contactDetails.emailAddress).toString,
            request.vrn,
            frontendAppConfig.feedbackUrl,
            showEmailConfirmation,
            commencementDate.format(dateFormatter),
            vatReturnEndDate.format(dateFormatter),
            vatReturnDeadline.format(dateFormatter),
            dateService.lastDayOfCalendarQuarter.format(dateFormatter),
            dateService.startOfCurrentQuarter.format(dateFormatter),
            dateService.startOfNextQuarter.format(dateFormatter),
            isDOFSDifferentToCommencementDate
          )
        )
      }
    }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  private def getStartDate(answers: UserAnswers): Option[LocalDate] =
    answers.get(DateOfFirstSalePage) match {
      case Some(startDate) => Some(dateService.startDateBasedOnFirstSale(startDate))
      case None            => Some(LocalDate.now())
    }
}
