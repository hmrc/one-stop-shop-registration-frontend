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

package viewmodels.checkAnswers

import formats.Format.dateFormatter
import logging.Logging
import models.UserAnswers
import models.requests.AuthenticatedDataRequest
import play.api.i18n.Messages
import services.{DateService, RegistrationService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommencementDateSummary @Inject()(dateService: DateService, registrationService: RegistrationService) extends Logging {

  def row(answers: UserAnswers)
         (implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[SummaryListRow] = {

    for {
      calculatedCommencementDate <- if (registrationService.eligibleSalesDifference(request.registration, answers)) {
        dateService.calculateCommencementDate(answers)
      } else {
        request.registration match {
          case Some(registration) => Future.successful(registration.commencementDate)
          case _ => val exception = new IllegalStateException("Registration was expected but not found")
            logger.error(exception.getMessage, exception)
            throw exception
        }
      }
    } yield {

      SummaryListRowViewModel(
        key = "commencementDate.checkYourAnswersLabel",
        value = ValueViewModel(calculatedCommencementDate.format(dateFormatter)),
        actions = Seq.empty
      )
    }
  }
}