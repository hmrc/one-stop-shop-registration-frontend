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

import config.Constants
import controllers.actions._
import forms.TradingNameFormProvider
import models.{Index, Mode}
import pages.TradingNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.AllTradingNames
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TradingNameController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: TradingNameFormProvider,
                                        view: TradingNameView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (cc.authAndGetData andThen cc.limitIndex(index, Constants.maxTradingNames)) {
      implicit request =>

        val form = formProvider(index, request.userAnswers.get(AllTradingNames).getOrElse(Seq.empty))

        val preparedForm = request.userAnswers.get(TradingNamePage(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, mode, index))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] =
    (cc.authAndGetData andThen cc.limitIndex(index, Constants.maxTradingNames)).async {
      implicit request =>

        val form = formProvider(index, request.userAnswers.get(AllTradingNames).getOrElse(Seq.empty))

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, index))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TradingNamePage(index), value))
              _              <- cc.sessionRepository.set(updatedAnswers)
            } yield Redirect(TradingNamePage(index).navigate(mode, updatedAnswers))
        )
    }
}
