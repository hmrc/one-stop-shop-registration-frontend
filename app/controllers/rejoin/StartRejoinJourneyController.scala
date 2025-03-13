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

import connectors.{RegistrationConnector, ReturnStatusConnector}
import controllers.CheckOutstandingReturns.existsOutstandingReturns
import controllers.actions.*
import logging.Logging
import models.RejoinMode
import models.domain.Registration
import models.requests.AuthenticatedMandatoryDataRequest
import models.responses.ErrorResponse
import pages.{DateOfFirstSalePage, HasMadeSalesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.AuthenticatedUserAnswersRepository
import services.{RegistrationService, RejoinEuRegistrationValidationService, RejoinPreviousRegistrationValidationService, RejoinRegistrationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartRejoinJourneyController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              cc: AuthenticatedControllerComponents,
                                              registrationConnector: RegistrationConnector,
                                              returnStatusConnector: ReturnStatusConnector,
                                              registrationService: RegistrationService,
                                              rejoinRegistrationService: RejoinRegistrationService,
                                              rejoinPreviousRegistrationValidationService: RejoinPreviousRegistrationValidationService,
                                              rejoinEuRegistrationValidationService: RejoinEuRegistrationValidationService,
                                              authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository,
                                              clock: Clock
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetDataWithOss(Some(RejoinMode)).async {
    implicit request =>
      returnStatusConnector.getCurrentReturns(request.vrn).flatMap{currentReturnResponse =>
        val currentReturns = getResponseValue(currentReturnResponse)
        val registration: Registration = request.registration
        
        if (rejoinRegistrationService.canRejoinRegistration(LocalDate.now(clock), registration.excludedTrader)&& !existsOutstandingReturns(currentReturns, clock)) {
          validateRegistration(registration).flatMap {
            case Some(redirect) => redirect.toFuture
            case None => registrationConnector.getVatCustomerInfo().flatMap {
              case Right(vatInfo) =>
                vatInfo.deregistrationDecisionDate match {
                  case Some(_) =>
                    Redirect(controllers.rejoin.routes.CannotRejoinController.onPageLoad().url).toFuture
                  case None =>
                    for {
                      userAnswers <- registrationService.toUserAnswers(request.userId, registration, vatInfo)
                      updatedAnswers <- Future.fromTry(userAnswers.remove(HasMadeSalesPage))
                      updateAnswers2 <- Future.fromTry(updatedAnswers.remove(DateOfFirstSalePage))
                      _ <- authenticatedUserAnswersRepository.set(updateAnswers2)
                    } yield Redirect(controllers.routes.HasMadeSalesController.onPageLoad(RejoinMode).url)
                }

              case Left(error) =>
                val exception = new Exception(error.body)
                logger.error(exception.getMessage, exception)
                throw exception
            }
          }
        } else {
          Redirect(controllers.rejoin.routes.CannotRejoinController.onPageLoad().url).toFuture
        }
      }
  }


  private def validateRegistration(registration: Registration)
                                  (implicit hc: HeaderCarrier, request: AuthenticatedMandatoryDataRequest[_]): Future[Option[Result]] = {
    rejoinPreviousRegistrationValidationService.validatePreviousRegistrations(registration.previousRegistrations).flatMap {
      case Some(redirect) => Some(redirect).toFuture
      case None => rejoinEuRegistrationValidationService.validateEuRegistrations(registration.euRegistrations)
    }
  }

  private def getResponseValue[A](response: Either[ErrorResponse, A]): A = {
    response match {
      case Right(value) => value
      case Left(error) =>
        val exception = new Exception(error.body)
        logger.error(exception.getMessage, exception)
        throw exception
    }
  }
}

