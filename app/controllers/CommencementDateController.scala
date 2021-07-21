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
              Ok(view(mode, startDate.format(dateFormatter)))
          }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case _ =>
          request.userAnswers.get(IsPlanningFirstEligibleSalePage) match {
            case Some(true) =>
              val startDate = LocalDate.now()
              Ok(view(mode, startDate.format(dateFormatter)))
          } case _ => Redirect(routes.RegisterLaterController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>
      Redirect(CommencementDatePage.navigate(mode, request.userAnswers))
  }
}
