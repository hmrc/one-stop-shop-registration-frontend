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

import controllers.actions._
import formats.Format.dateFormatter
import logging.Logging
import models.Mode
import pages.{CommencementDatePage, DateOfFirstSalePage, HasMadeSalesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DateService, RegistrationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckJourneyRecovery.determineJourneyRecovery
import views.html.CommencementDateView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CommencementDateController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            cc: AuthenticatedControllerComponents,
                                            view: CommencementDateView,
                                            dateService: DateService,
                                            registrationService: RegistrationService
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      val hasMadeSales = request.userAnswers.get(HasMadeSalesPage).contains(true)
      for {
        maybeCalculatedCommencementDate <- dateService.calculateCommencementDate(request.userAnswers)
        calculatedCommencementDate = maybeCalculatedCommencementDate.getOrElse {
          val exception = new IllegalStateException("A calculated commencement date is expected")
          logger.error(exception.getMessage, exception)
          throw exception
        }
        isEligibleSalesAmendable <- registrationService.isEligibleSalesAmendable(mode)
        finalDayOfDateAmendment = dateService.calculateFinalAmendmentDate(calculatedCommencementDate)
      } yield {
        if (isEligibleSalesAmendable) {
          request.userAnswers.get(HasMadeSalesPage) match {
            case Some(true) =>
              request.userAnswers.get(DateOfFirstSalePage).map { _ =>
                  val endOfCurrentQuarter = dateService.lastDayOfCalendarQuarter
                  val isDateInCurrentQuarter = calculatedCommencementDate.isBefore(endOfCurrentQuarter) || endOfCurrentQuarter == calculatedCommencementDate
                  val startOfCurrentQuarter = dateService.startOfCurrentQuarter
                  val startOfNextQuarter = dateService.startOfNextQuarter()

                  Ok(
                    view(
                      mode,
                      calculatedCommencementDate.format(dateFormatter),
                      finalDayOfDateAmendment.format(dateFormatter),
                      isDateInCurrentQuarter,
                      Some(startOfCurrentQuarter.format(dateFormatter)),
                      Some(endOfCurrentQuarter.format(dateFormatter)),
                      Some(startOfNextQuarter.format(dateFormatter)),
                      hasMadeSales
                    )
                  )
              }.getOrElse(Redirect(determineJourneyRecovery(Some(mode))))

            case Some(false) =>
              Ok(view(mode,
                calculatedCommencementDate.format(dateFormatter),
                finalDayOfDateAmendment.format(dateFormatter),
                isDateInCurrentQuarter = true,
                None,
                None,
                None,
                hasMadeSales
              ))

            case _ => Redirect(determineJourneyRecovery(Some(mode)))
          }
        } else {
          Redirect(CommencementDatePage.navigate(mode, request.userAnswers))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)) {
    implicit request =>
      Redirect(CommencementDatePage.navigate(mode, request.userAnswers))
  }
}
