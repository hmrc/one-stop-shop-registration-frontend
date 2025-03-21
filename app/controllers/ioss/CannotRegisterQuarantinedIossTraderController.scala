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

package controllers.ioss

import controllers.actions.*
import formats.Format.quarantinedIOSSRegistrationFormatter
import logging.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ioss.IossExclusionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ioss.CannotRegisterQuarantinedIossTraderView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CannotRegisterQuarantinedIossTraderController @Inject()(
                                                               override val messagesApi: MessagesApi,
                                                               cc: AuthenticatedControllerComponents,
                                                               iossExclusionService: IossExclusionService,
                                                               view: CannotRegisterQuarantinedIossTraderView
                                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = (cc.actionBuilder andThen cc.identify).async {
    implicit request =>
      request.iossNumber match {
        case Some(iossNumber) =>
          iossExclusionService.getIossEtmpExclusion(iossNumber).map {
            case Some(iossEtmpExclusion) =>
              val excludeEndDate: String = iossEtmpExclusion.effectiveDate.plusYears(2).plusDays(1).format(quarantinedIOSSRegistrationFormatter)
              Ok(view(excludeEndDate))
            case _ =>
              val exception = new IllegalStateException("Expected an ETMP Exclusion")
              logger.error(s"Service was unable to retrieve ETMP Exclusion: ${exception.getMessage}", exception)
              throw exception
          }

        case _ =>
          val exception = new IllegalStateException("Expected an IOSS number")
          logger.error(s"Request does not contain an IOSS number: ${exception.getMessage}", exception)
          throw exception
      }
  }
}
