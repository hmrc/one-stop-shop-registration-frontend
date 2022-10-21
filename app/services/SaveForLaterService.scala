/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import connectors.{SaveForLaterConnector, SavedUserAnswers}
import logging.Logging
import models.SessionData
import models.requests.{AuthenticatedDataRequest, SaveForLaterRequest}
import pages.SavedProgressPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SaveForLaterService @Inject()(
                                     sessionRepository: SessionRepository,
                                     saveForLaterConnector: SaveForLaterConnector
                                   )(implicit val ec: ExecutionContext, implicit val hc: HeaderCarrier) extends Logging {


  def saveAnswers(
                   sessionId: String,
                   redirectLocation: Call,
                   originLocation: Call,
                   errorLocation: Call
                 )(implicit request: AuthenticatedDataRequest[_]): Future[Result] = {
    Future.fromTry(request.userAnswers.set(SavedProgressPage, originLocation.url)).flatMap {
      updatedAnswers =>
//        val sessionId = request.queryString
//          .get("k")
//          .flatMap(_.headOption)
//          .orElse(hc.sessionId.map(_.value))
//          .map(sessionId => sessionId)
        val save4LaterRequest = SaveForLaterRequest(updatedAnswers, request.vrn)
        saveForLaterConnector.submit(save4LaterRequest).flatMap {
          case Right(Some(_: SavedUserAnswers)) =>
            for {
              sessionData <- sessionRepository.get(sessionId)
              updatedSessionData = sessionData.headOption.fold(SessionData(request.userId))(_.copy(userId = request.userId))
              _ <- sessionRepository.set(updatedSessionData)
            } yield {
              Redirect(redirectLocation)
            }
          case Right(None) =>
            logger.error(s"Unexpected result on submit")
            Future.successful(Redirect(errorLocation))
          case Left(e) =>
            logger.error(s"Unexpected result on submit: ${e.toString}")
            Future.successful(Redirect(errorLocation))
        }
    }

  }


}
