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

import controllers.actions._
import formats.Format.dateFormatter
import models.Mode
import pages.{CommencementDatePage, DateOfFirstSalePage, HasMadeSalesPage, IsPlanningFirstEligibleSalePage}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CommencementDateView

import java.time.LocalDate

class CommencementDateController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       view: CommencementDateView,
                                       dateService: DateService,
                                     ) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>
      request.userAnswers.get(HasMadeSalesPage) match {
        case Some(true) =>
          request.userAnswers.get(DateOfFirstSalePage).map {
            date =>
              val startDate = dateService.startDateBasedOnFirstSale(date)

              val registrationDate = dateService.getRegistrationDate()
              val isRegisteredAfterThe10th = dateService.isRegistrationDateAfter10thOfTheMonth(registrationDate)

              val isStartDateInFirstQuarter = dateService.isStartDateInFirstQuarter(startDate)
              val isStartDateAfterFirstQuarter = dateService.isStartDateAfterFirstQuarter(startDate)
              Ok(view(mode, startDate.format(dateFormatter), isRegisteredAfterThe10th, isStartDateInFirstQuarter, isStartDateAfterFirstQuarter))
          }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(false) =>
          request.userAnswers.get(IsPlanningFirstEligibleSalePage) match {
            case Some(true) =>
              val plannedStartDate = LocalDate.now()
              Ok(view(mode, plannedStartDate.format(dateFormatter), false, false, false))
            case Some(false) => Redirect(routes.RegisterLaterController.onPageLoad())
          }
        case _ => Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>
      Redirect(CommencementDatePage.navigate(mode, request.userAnswers))
  }
}

//Scenario 1
//From 11 August 2021 onwards,
//if they register after 10th of August (if today > 10th of month)
// and their first sale is after 1 August, (user entered date after 01st of month)
// we tell them that their start date is date they've given us.

//OR

//From 1 September 2021 onwards,
// if they're registering between 1st and 10th September
// and their first sale is after 1 August,
// we tell them that their start date is date they've given us.



//Scenario 2
//From 11 August 2021 onwards,
// if they register for the scheme after 10th August
// and their first sale is before 1 August,
// we tell them that their start date is first day of next quarter.

//OR

//From 1 September 2021 onwards,
// if they register for the scheme between 1st and 10th September
// and their first sale is before 1st August,
// we tell them that their start date is first day of next quarter.


// Scenario 3 covered by isPlanned route