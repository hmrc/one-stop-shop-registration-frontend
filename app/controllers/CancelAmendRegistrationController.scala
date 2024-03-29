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

import config.FrontendAppConfig
import controllers.actions._
import models.Mode
import logging.Logging
import forms.CancelAmendRegFormProvider
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CancelAmendRegistrationView

import scala.concurrent.{ExecutionContext, Future}

class CancelAmendRegistrationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       formProvider: CancelAmendRegFormProvider,
                                       appConfig: FrontendAppConfig,
                                       view: CancelAmendRegistrationView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging{
  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)){
    implicit request =>
      
      Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          if (value) {
            for {
              _ <- cc.sessionRepository.clear(request.userId)
            } yield Redirect(appConfig.ossYourAccountUrl)
          } else {
            Future.successful(Redirect(controllers.amend.routes.ChangeYourRegistrationController.onPageLoad()))
          }
      )
  }
}
