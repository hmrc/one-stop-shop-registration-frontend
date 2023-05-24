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

import cats.data.Validated.Valid
import connectors.RegistrationConnector
import controllers.actions._
import controllers.amend.{routes => amendRoutes}
import logging.Logging
import models.{AmendMode, Country, Index, UserAnswers}
import models.euDetails.{EuConsumerSalesMethod, EuDetails}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{AllEuDetailsQuery, EuDetailsQuery}
import repositories.AuthenticatedUserAnswersRepository
import services.{RegistrationService, RegistrationValidationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.DeleteAllFixedEstablishmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DeleteAllFixedEstablishmentController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       cc: AuthenticatedControllerComponents,
                                                       registrationConnector: RegistrationConnector,
                                                       registrationService: RegistrationService,
                                                       registrationValidationService: RegistrationValidationService,
                                                       view: DeleteAllFixedEstablishmentView,
                                                       authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetOptionalData(Some(AmendMode)).async {
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
                  _ <- authenticatedUserAnswersRepository.set(userAnswers)
                } yield {
                  val euDetails = getEuDetailsForFixedEstablishment(userAnswers)
                  Ok(view(euDetails))
                }

              case Left(error) => val exception = new Exception(s"TODO ${error}") // TODO
                logger.error(exception.getMessage, exception)
                throw exception
            }

          case _ => Redirect(routes.NotRegisteredController.onPageLoad()).toFuture
        }
      }).flatten
  }

  def onSubmit(): Action[AnyContent] = cc.authAndGetData(Some(AmendMode)).async {
    implicit request =>

      val futureUserAnswers = Future.fromTry(deleteFixedEstablishment(request.userAnswers))

      (for {
        userAnswers <- futureUserAnswers
        _ <- authenticatedUserAnswersRepository.set(userAnswers)
      } yield {
        registrationValidationService.fromUserAnswers(userAnswers, request.vrn).flatMap {
          case Valid(registration) =>
            registrationConnector.amendRegistration(registration).flatMap {
              case Right(_) => Redirect(amendRoutes.AmendCompleteController.onPageLoad()).toFuture

              case Left(e) =>
                logger.error(s"Unexpected result on submit: ${e.toString}")
                Redirect(amendRoutes.ErrorSubmittingAmendmentController.onPageLoad()).toFuture
            }
          case _ =>
            logger.error("Error while validating registration")
            throw new Exception("Error while validating registration")
        }
      }).flatten
  }

  private def getEuDetailsForFixedEstablishment(userAnswers: UserAnswers): Seq[EuDetails] = {

    userAnswers.get(AllEuDetailsQuery).getOrElse(Seq.empty).filter(_.sellsGoodsToEUConsumerMethod.contains(EuConsumerSalesMethod.FixedEstablishment))

  }

  private def deleteFixedEstablishment(userAnswers: UserAnswers): Try[UserAnswers] = {

    val allEuDetails = userAnswers.get(AllEuDetailsQuery).getOrElse(Seq.empty)

    def recursivelyRemoveEuDetails(currentUserAnswers: UserAnswers, remainingEuDetails: Seq[EuDetails]): Try[UserAnswers] = {

      remainingEuDetails match {
        case Nil => Try(currentUserAnswers)
        case firstEuDetail :: Nil if firstEuDetail.sellsGoodsToEUConsumerMethod.contains(EuConsumerSalesMethod.FixedEstablishment) =>
          removeUserAnswer(currentUserAnswers, calculateIndexOfCountry(firstEuDetail.euCountry, currentUserAnswers))
        case _ :: Nil =>
          Try(currentUserAnswers)
        case firstEuDetail :: otherEuDetails if firstEuDetail.sellsGoodsToEUConsumerMethod.contains(EuConsumerSalesMethod.FixedEstablishment) =>
          removeUserAnswer(currentUserAnswers, calculateIndexOfCountry(firstEuDetail.euCountry, currentUserAnswers)).flatMap { cleanedUserAnswers =>
            recursivelyRemoveEuDetails(cleanedUserAnswers, otherEuDetails)
          }
        case _ :: otherEuDetails =>
          recursivelyRemoveEuDetails(currentUserAnswers, otherEuDetails)
      }
    }

    recursivelyRemoveEuDetails(userAnswers, allEuDetails)
  }

  private def calculateIndexOfCountry(country: Country, userAnswers: UserAnswers): Int = {
    val currentEuDetails = userAnswers.get(AllEuDetailsQuery).getOrElse(Seq.empty)

    currentEuDetails.indexWhere(_.euCountry == country)
  }

  private def removeUserAnswer(currentUserAnswers: UserAnswers, countryIndex: Int): Try[UserAnswers] = {
    currentUserAnswers.remove(EuDetailsQuery(Index(countryIndex)))
  }

}
