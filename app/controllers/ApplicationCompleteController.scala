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

import config.FrontendAppConfig
import controllers.actions._
import pages.BusinessContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import queries.EmailConfirmationQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationCompleteView

import javax.inject.Inject

class ApplicationCompleteController @Inject()(
  override val messagesApi: MessagesApi,
  cc: AuthenticatedControllerComponents,
  view: ApplicationCompleteView,
  frontendAppConfig: FrontendAppConfig
) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = (cc.identify andThen cc.getData andThen cc.requireData) {
    implicit request =>
      val businessContactDetailsPage = request.userAnswers.get(BusinessContactDetailsPage)
      val showEmailConfirmation = request.userAnswers.get(EmailConfirmationQuery)

      Ok(view(
        HtmlFormat.escape(businessContactDetailsPage.get.emailAddress).toString,
        request.vrn,
        frontendAppConfig.feedbackUrl,
        showEmailConfirmation.getOrElse(false)
      ))
  }
}
