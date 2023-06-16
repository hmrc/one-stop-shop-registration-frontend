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
import forms.IsPlanningFirstEligibleSaleFormProvider

import javax.inject.Inject
import models.Mode
import pages.IsPlanningFirstEligibleSalePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IsPlanningFirstEligibleSaleView

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}

class IsPlanningFirstEligibleSaleController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: IsPlanningFirstEligibleSaleFormProvider,
                                         view: IsPlanningFirstEligibleSaleView,
                                         dateService: DateService,
                                         clock: Clock
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = (cc.authAndGetData(Some(mode)) andThen cc.checkEligibleSalesAmendable(Some(mode))) {
    implicit request =>

      val maybeRegistrationDate = request.registration.flatMap(_.submissionReceived.map(_.atZone(clock.getZone).toLocalDate))
      val form = formProvider(maybeRegistrationDate)
      val firstDayOfNextCalendarQuarter = dateService.startOfNextQuarter()

      val preparedForm = request.userAnswers.get(IsPlanningFirstEligibleSalePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, firstDayOfNextCalendarQuarter.format(dateFormatter)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (cc.authAndGetData(Some(mode)) andThen cc.checkEligibleSalesAmendable(Some(mode))).async {
    implicit request =>

      val maybeRegistrationDate = request.registration.flatMap(_.submissionReceived.map(_.atZone(clock.getZone).toLocalDate))
      val form = formProvider(maybeRegistrationDate)
      val firstDayOfNextCalendarQuarter = dateService.startOfNextQuarter()

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, firstDayOfNextCalendarQuarter.format(dateFormatter)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IsPlanningFirstEligibleSalePage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(IsPlanningFirstEligibleSalePage.navigate(mode, updatedAnswers))
        )
  }
}
