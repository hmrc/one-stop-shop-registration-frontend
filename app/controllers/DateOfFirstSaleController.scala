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
import formats.Format.{dateFormatter, dateHintFormatter}
import forms.DateOfFirstSaleFormProvider

import javax.inject.Inject
import models.Mode
import pages.DateOfFirstSalePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DateOfFirstSaleView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class DateOfFirstSaleController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: DateOfFirstSaleFormProvider,
                                        view: DateOfFirstSaleView,
                                        dateService: DateService
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form: Form[LocalDate] = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(DateOfFirstSalePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val earliestDateFormatted     = dateService.earliestSaleAllowed.format(dateFormatter)
      val earliestDateHintFormatted = dateService.earliestSaleAllowed.format(dateHintFormatter)

      Ok(view(preparedForm, mode, earliestDateFormatted, earliestDateHintFormatted))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val earliestDateFormatted     = dateService.earliestSaleAllowed.format(dateFormatter)
          val earliestDateHintFormatted = dateService.earliestSaleAllowed.format(dateHintFormatter)

          Future.successful(BadRequest(view(formWithErrors, mode, earliestDateFormatted, earliestDateHintFormatted)))
        },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DateOfFirstSalePage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(DateOfFirstSalePage.navigate(mode, updatedAnswers))
      )
  }
}
