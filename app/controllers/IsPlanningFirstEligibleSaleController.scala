/*
 * Copyright 2022 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}

class IsPlanningFirstEligibleSaleController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: IsPlanningFirstEligibleSaleFormProvider,
                                         view: IsPlanningFirstEligibleSaleView,
                                         dateService: DateService
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val firstDayOfNextCalendarQuarter = dateService.startOfNextQuarter

      val preparedForm = request.userAnswers.get(IsPlanningFirstEligibleSalePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, firstDayOfNextCalendarQuarter.format(dateFormatter)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      val firstDayOfNextCalendarQuarter = dateService.startOfNextQuarter

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
