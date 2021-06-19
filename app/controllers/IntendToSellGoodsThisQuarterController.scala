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
import forms.IntendToSellGoodsThisQuarterFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import pages.{CommencementDatePage, IntendToSellGoodsThisQuarterPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.StartDateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IntendToSellGoodsThisQuarterView

import java.time.{Clock, LocalDate}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class IntendToSellGoodsThisQuarterController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: IntendToSellGoodsThisQuarterFormProvider,
                                         view: IntendToSellGoodsThisQuarterView,
                                         clock: Clock,
                                         startDateService: StartDateService
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(IntendToSellGoodsThisQuarterPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(updateUserAnswers(value, request.userAnswers))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(IntendToSellGoodsThisQuarterPage.navigate(mode, updatedAnswers))
      )
  }

  private def updateUserAnswers(answer: Boolean, answers: UserAnswers): Try[UserAnswers] =
    if (answer) {
      answers
        .set(IntendToSellGoodsThisQuarterPage, answer)
        .flatMap(_.set(CommencementDatePage, startDateService.startDateBasedOnIntentionToSellGoods()))
    } else {
      answers.set(IntendToSellGoodsThisQuarterPage, answer)
    }
}
