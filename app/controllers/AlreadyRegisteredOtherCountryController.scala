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
import connectors.RegistrationConnector
import models.Country
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AlreadyRegisteredOtherCountryView

import scala.concurrent.ExecutionContext
import javax.inject.Inject

class AlreadyRegisteredOtherCountryController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       view: AlreadyRegisteredOtherCountryView,
                                       connector: RegistrationConnector
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(countryCode: String): Action[AnyContent] = (cc.actionBuilder andThen cc.identify).async {
    implicit request =>

      connector.getSavedExternalEntry().map {
        case Right(response) =>
          Ok(view(Country.getCountryName(countryCode), response.url))
        case Left(_) =>
          Ok(view(Country.getCountryName(countryCode), None))
      }
  }
}
