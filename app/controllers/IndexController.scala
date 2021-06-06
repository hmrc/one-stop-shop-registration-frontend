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

import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import models.{NormalMode, UserAnswers, responses}
import navigation.Navigator
import pages.FirstAuthedPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 override val messagesApi: MessagesApi,
                                 cc: AuthenticatedControllerComponents,
                                 navigator: Navigator,
                                 connector: RegistrationConnector,
                                 clock: Clock,
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = (cc.identify andThen cc.checkRegistration andThen cc.getData).async {
    implicit request =>

      request.userAnswers match {
        case Some(answers) =>
          Future.successful(Redirect(navigator.nextPage(FirstAuthedPage, NormalMode, answers)))

        case None =>
          connector.getVatCustomerInfo().flatMap {
            case Right(vatInfo) =>
              val answers = UserAnswers(request.userId, vatInfo = Some(vatInfo), lastUpdated = Instant.now(clock))
              cc.sessionRepository.set(answers).map {
                _ =>
                  Redirect(navigator.nextPage(FirstAuthedPage, NormalMode, answers))
              }

            case Left(responses.NotFound) =>
              val answers = UserAnswers(request.userId, vatInfo = None, lastUpdated = Instant.now(clock))
              cc.sessionRepository.set(answers).map {
                _ =>
                  Redirect(navigator.nextPage(FirstAuthedPage, NormalMode, answers))
              }

            case Left(_) =>
              Future.successful(InternalServerError)
          }
      }
  }
}
