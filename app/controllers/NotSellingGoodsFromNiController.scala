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

import connectors.RegistrationConnector
import controllers.actions._
import logging.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NotSellingGoodsFromNiView

import scala.concurrent.ExecutionContext

class NotSellingGoodsFromNiController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: UnauthenticatedControllerComponents,
                                       registrationConnector: RegistrationConnector,
                                       view: NotSellingGoodsFromNiView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = (cc.actionBuilder andThen cc.identify).async {
    implicit request =>

      registrationConnector.getSavedExternalEntry().map {
        case Right(response) =>
          Ok(view(response.url))
        case Left(e) =>
          logger.warn(s"There was an error when getting saved external entry url ${e.body} but we didn't block the user from continuing the journey")
          Ok(view(None))
      }
  }
}
