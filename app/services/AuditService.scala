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

package services

import config.FrontendAppConfig
import models.audit.JsonAuditModel
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(
                              appConfig: FrontendAppConfig,
                              auditConnector: AuditConnector
                            )(implicit ec: ExecutionContext) {

  def audit(dataSource: JsonAuditModel)(implicit hc: HeaderCarrier, request: Request[_]): Unit = {
    val event = toExtendedDataEvent(dataSource, request.path)
    // This will discard failures and also potentially not finish execution before the result is shown
    auditConnector.sendExtendedEvent(event)
  }

  // Future[Unit] hides things like Future[Future[Unit]] leading to errors not cascading properly, race conditions in
  // tests as .futureValue will only wait for the outer future. A bit pedantic but can lead to confusion.
  def audit2(dataSource: JsonAuditModel)(implicit hc: HeaderCarrier, request: Request[_]): Future[Boolean] = {
    val event = toExtendedDataEvent(dataSource, request.path)
    auditConnector.sendExtendedEvent(event).map(_ => true).recover {
      // explicit discarding of error, also we can add a comment why we do not care about the error not being able to
      // actually be visible with a 500 or whatever.
      case   _: Throwable => true
    }
  }



  private def toExtendedDataEvent(auditModel: JsonAuditModel, path: String)(implicit hc: HeaderCarrier): ExtendedDataEvent =
    ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType   = auditModel.auditType,
      tags        = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(auditModel.transactionName, path),
      detail      = auditModel.detail
    )
}

