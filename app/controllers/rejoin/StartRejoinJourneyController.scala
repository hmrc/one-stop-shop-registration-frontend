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

import connectors.RegistrationConnector
import controllers.actions._
import logging.Logging
import models.RejoinMode
import pages.HasMadeSalesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.AuthenticatedUserAnswersRepository
import services.{RegistrationService, RejoinPreviousRegistrationValidationService, RejoinRegistrationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartRejoinJourneyController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              cc: AuthenticatedControllerComponents,
                                              registrationConnector: RegistrationConnector,
                                              registrationService: RegistrationService,
                                              rejoinRegistrationService: RejoinRegistrationService,
                                              rejoinPreviousRegistrationValidationService: RejoinPreviousRegistrationValidationService,
                                              authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository,
                                              clock: Clock
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetOptionalData(Some(RejoinMode)).async {
    implicit request =>
      (for {
        maybeRegistration <- registrationConnector.getRegistration()
      } yield {
        maybeRegistration match {
          case Some(registration) if rejoinRegistrationService.canRejoinRegistration(LocalDate.now(clock), registration.excludedTrader) =>
            // Use validation result
            rejoinPreviousRegistrationValidationService.validatePreviousRegistrations(registration.previousRegistrations)

            registrationConnector.getVatCustomerInfo().flatMap {
              case Right(vatInfo) =>
                for {
                  userAnswers <- registrationService.toUserAnswers(request.userId, registration, vatInfo)
                  updatedAnswers <- Future.fromTry(userAnswers.remove(HasMadeSalesPage))
                  _ <- authenticatedUserAnswersRepository.set(updatedAnswers)
                } yield Redirect(controllers.routes.HasMadeSalesController.onPageLoad(RejoinMode).url)

              case Left(error) =>
                val exception = new Exception(error.body)
                logger.error(exception.getMessage, exception)
                throw exception
            }
          case _ =>
            Redirect(controllers.rejoin.routes.CannotRejoinController.onPageLoad().url).toFuture
        }
      }).flatten
  }

}
