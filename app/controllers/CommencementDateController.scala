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

import controllers.actions._
import formats.Format.dateFormatter
import models.Mode
import pages.{CommencementDatePage, DateOfFirstSalePage, HasMadeSalesPage, IsPlanningFirstEligibleSalePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CommencementDateView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CommencementDateController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            cc: AuthenticatedControllerComponents,
                                            view: CommencementDateView,
                                            dateService: DateService
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      for {
        calculatedCommencementDate <- dateService.calculateCommencementDate(request.userAnswers)
      } yield {
        request.userAnswers.get(HasMadeSalesPage) match {
          case Some(true) =>
            request.userAnswers.get(DateOfFirstSalePage).map {
              date =>

                val endOfCurrentQuarter = dateService.lastDayOfCalendarQuarter
                val isDateInCurrentQuarter = calculatedCommencementDate.isBefore(endOfCurrentQuarter) || endOfCurrentQuarter == calculatedCommencementDate
                val startOfCurrentQuarter = dateService.startOfCurrentQuarter
                val startOfNextQuarter = dateService.startOfNextQuarter

                Ok(
                  view(
                    mode,
                    calculatedCommencementDate.format(dateFormatter),
                    isDateInCurrentQuarter,
                    Some(startOfCurrentQuarter.format(dateFormatter)),
                    Some(endOfCurrentQuarter.format(dateFormatter)),
                    Some(startOfNextQuarter.format(dateFormatter))
                  )
                )
            }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))

          case Some(false) =>
            request.userAnswers.get(IsPlanningFirstEligibleSalePage) match {
              case Some(true) =>
                Ok(view(mode, calculatedCommencementDate.format(dateFormatter), isDateInCurrentQuarter = true, None, None, None))
              case Some(false) => Redirect(routes.RegisterLaterController.onPageLoad())
              case _ => Redirect(routes.JourneyRecoveryController.onPageLoad())
            }

          case _ => Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>
      Redirect(CommencementDatePage.navigate(mode, request.userAnswers))
  }
}
