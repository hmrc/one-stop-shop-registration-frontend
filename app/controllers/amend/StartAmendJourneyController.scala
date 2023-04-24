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

package controllers.amend

import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import models.AmendMode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{AuthenticatedUserAnswersRepository, SessionRepository}
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class StartAmendJourneyController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             cc: AuthenticatedControllerComponents,
                                             registrationConnector: RegistrationConnector,
                                             registrationService: RegistrationService,
                                             authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository
                                           )(implicit ec: ExecutionContext)
  extends FrontendBaseController with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetOptionalData(Some(AmendMode)).async { // TODO require HMRC-OSS-ORG enrolment key
    implicit request =>
      for {
        maybeRegistration <- registrationConnector.getRegistration()
        registration = maybeRegistration.getOrElse(throw new Exception("TODO")) // TODO
        maybeVatCustomerInfo <- registrationConnector.getVatCustomerInfo()
        vatCustomerInfo = maybeVatCustomerInfo match {
          case Right(vci) => vci
          case Left(error) => val exception = new Exception(s"TODO ${error}") // TODO
            logger.error(exception.getMessage, exception)
            throw exception
        }
        userAnswers <- registrationService.toUserAnswers(request.userId, registration, vatCustomerInfo)
        _ <- authenticatedUserAnswersRepository.set(userAnswers)
      } yield
        Redirect(routes.ChangeYourRegistrationController.onPageLoad().url)

  }

}
