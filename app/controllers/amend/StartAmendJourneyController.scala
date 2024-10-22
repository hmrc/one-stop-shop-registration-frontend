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

package controllers.amend

import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import models.AmendMode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.OriginalRegistrationQuery
import repositories.AuthenticatedUserAnswersRepository
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartAmendJourneyController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             cc: AuthenticatedControllerComponents,
                                             registrationConnector: RegistrationConnector,
                                             registrationService: RegistrationService,
                                             authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository
                                           )(implicit ec: ExecutionContext)
  extends FrontendBaseController with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetOptionalData(Some(AmendMode)).async {
    implicit request =>
      (for {
        maybeRegistration <- registrationConnector.getRegistration()
      } yield {
        maybeRegistration match {
          case Some(registration) =>
            registrationConnector.getVatCustomerInfo().flatMap {
              case Right(vatInfo) =>
                for {
                  userAnswers <- registrationService.toUserAnswers(request.userId, registration, vatInfo)
                  originalRegistration <- Future.fromTry(userAnswers.set(OriginalRegistrationQuery, registration))
                  _ <- authenticatedUserAnswersRepository.set(userAnswers)
                  _ <- authenticatedUserAnswersRepository.set(originalRegistration)
                } yield Redirect(routes.ChangeYourRegistrationController.onPageLoad().url)

              case Left(error) => val exception = new Exception(s"Failed to retrieve VAT information when starting amend $error")
                logger.error(exception.getMessage, exception)
                throw exception
            }

          case None =>
            Redirect(controllers.routes.NotRegisteredController.onPageLoad().url).toFuture
        }
      }).flatten
  }
}
