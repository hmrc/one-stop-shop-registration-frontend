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

package controllers.rejoin

import config.Constants.addQuarantineYears
import config.FrontendAppConfig
import controllers.actions._
import formats.Format.dateFormatter
import models.Country.getCountryName
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rejoin.CannotRejoinQuarantinedCountryView

import java.time.LocalDate
import javax.inject.Inject

class CannotRejoinQuarantinedCountryController @Inject()(
                                                          override val messagesApi: MessagesApi,
                                                          cc: AuthenticatedControllerComponents,
                                                          frontendAppConfig: FrontendAppConfig,
                                                          view: CannotRejoinQuarantinedCountryView
                                                        ) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(countryCode: String, exclusionDate: String): Action[AnyContent] = (cc.actionBuilder andThen cc.identify) { implicit request =>
    val exclusionDateFormatted = LocalDate.parse(exclusionDate).plusYears(addQuarantineYears).format(dateFormatter)

    Ok(view(frontendAppConfig.ossYourAccountUrl, getCountryName(countryCode), exclusionDateFormatted))
  }
}
