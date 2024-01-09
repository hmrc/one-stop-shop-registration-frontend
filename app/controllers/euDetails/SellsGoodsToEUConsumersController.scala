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

package controllers.euDetails

import controllers.GetCountry
import controllers.actions._
import forms.euDetails.SellsGoodsToEUConsumersFormProvider
import models.{Index, Mode}
import pages.euDetails.SellsGoodsToEUConsumersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.SellsGoodsToEUConsumersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SellsGoodsToEUConsumersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   cc: AuthenticatedControllerComponents,
                                                   formProvider: SellsGoodsToEUConsumersFormProvider,
                                                   view: SellsGoodsToEUConsumersView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with GetCountry {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCountry(mode, countryIndex) {

        country =>

          val form = formProvider(country)
          val preparedForm = request.userAnswers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, countryIndex, country)))
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCountry(mode, countryIndex) {

        country =>

          val form = formProvider(country)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, countryIndex, country))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(SellsGoodsToEUConsumersPage(countryIndex).navigate(mode, updatedAnswers))
          )
      }
  }
}
