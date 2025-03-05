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

package controllers.actions

import connectors.{RegistrationConnector, SaveForLaterConnector}
import models.UserAnswers
import models.requests.AuthenticatedOptionalDataRequest
import pages.SavedProgressPage
import play.api.mvc.ActionTransformer
import repositories.AuthenticatedUserAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SavedAnswersRetrievalAction(
                                   repository: AuthenticatedUserAnswersRepository,
                                   saveForLaterConnector: SaveForLaterConnector,
                                   registrationConnector: RegistrationConnector
                                 )
                                 (implicit val executionContext: ExecutionContext)
  extends ActionTransformer[AuthenticatedOptionalDataRequest, AuthenticatedOptionalDataRequest] {

  override protected def transform[A](request: AuthenticatedOptionalDataRequest[A]): Future[AuthenticatedOptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.request, request.request.session)
    val userAnswers =
      if (request.userAnswers.flatMap(_.get(SavedProgressPage)).isEmpty) {
        for {
          savedForLater <- saveForLaterConnector.get()
          maybeVatInfo <- registrationConnector.getVatCustomerInfo()
        } yield {
          val answers = {
            (savedForLater, maybeVatInfo) match {
              case (Right(Some(answers)), Right(vatInfo)) => {
                val newAnswers = UserAnswers(request.userId, answers.data, Some(vatInfo), answers.lastUpdated)
                repository.set(newAnswers)
                Some(newAnswers)
              }
              case _ => request.userAnswers
            }
          }
          answers
        }
      } else {
        Future.successful(request.userAnswers)
      }

    userAnswers.map {
      AuthenticatedOptionalDataRequest(request.request, request.credentials, request.vrn, request.registration, _)
    }
  }
}

class SavedAnswersRetrievalActionProvider @Inject()(
                                                     repository: AuthenticatedUserAnswersRepository,
                                                     saveForLaterConnector: SaveForLaterConnector,
                                                     registrationConnector: RegistrationConnector
                                                   )
                                                   (implicit ec: ExecutionContext) {

  def apply(): SavedAnswersRetrievalAction =
    new SavedAnswersRetrievalAction(repository, saveForLaterConnector, registrationConnector)
}
