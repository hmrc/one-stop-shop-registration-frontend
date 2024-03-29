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

import controllers.actions.{AuthenticatedControllerComponents, UnauthenticatedControllerComponents}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class KeepAliveController @Inject()(
                                     authCc: AuthenticatedControllerComponents,
                                     unauthCc: UnauthenticatedControllerComponents
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController {

  protected val controllerComponents: MessagesControllerComponents = authCc

  def keepAliveAuthenticated: Action[AnyContent] = (authCc.actionBuilder andThen authCc.identify andThen authCc.getData).async {
    implicit request =>
      request.userAnswers
        .map {
          answers =>
            authCc.sessionRepository.keepAlive(answers.id).map(_ => Ok)
        }.getOrElse(Future.successful(Ok))
  }

  def keepAliveUnauthenticated: Action[AnyContent] = unauthCc.identifyAndGetOptionalData.async {
    implicit request =>
      request.userAnswers
        .map {
          answers =>
            unauthCc.sessionRepository.keepAlive(answers.id).map(_ => Ok)
        }.getOrElse(Future.successful(Ok))
  }
}
