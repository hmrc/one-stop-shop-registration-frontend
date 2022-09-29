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

package controllers.test

import connectors.test.TestOnlyEmailPasscodeConnector
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyEmailPasscodeController @Inject()(
                                                 cc: AuthenticatedControllerComponents,
                                                 testOnlyEmailPasscodeConnector: TestOnlyEmailPasscodeConnector
                                               )(implicit ec: ExecutionContext) extends FrontendController(cc) with I18nSupport with Logging {

  def testOnlyGetPasscodes(): Action[AnyContent] = (cc.actionBuilder andThen cc.identify).async {
    implicit request =>
      logger.info("REQUEST: " + request)
      testOnlyEmailPasscodeConnector.getTestOnlyPasscode().flatMap {
        case Right(response) => Future.successful(Ok(response))
        case Left(error) => throw error
    }
  }
}
