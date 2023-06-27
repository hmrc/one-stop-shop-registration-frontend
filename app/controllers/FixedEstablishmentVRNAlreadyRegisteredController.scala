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

import controllers.actions._
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.EuDetailsQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.FixedEstablishmentVRNAlreadyRegisteredView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FixedEstablishmentVRNAlreadyRegisteredController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       view: FixedEstablishmentVRNAlreadyRegisteredView
                                     )(implicit executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index): Action[AnyContent] = (cc.actionBuilder andThen cc.identify) {
    implicit request =>
      Ok(view(mode, countryIndex))
  }

  def deleteAndRedirect(countryIndex: Index): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData).async {
    implicit request =>

      request.userAnswers.map {
        answers =>

          for {
            updatedAnswers <- Future.fromTry(answers.remove(EuDetailsQuery(countryIndex)))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield {
            Redirect(controllers.amend.routes.ChangeYourRegistrationController.onPageLoad())
          }
      }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)))
  }
}
