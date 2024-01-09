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

package controllers.euDetails

import controllers.actions._
import models.requests.AuthenticatedDataRequest
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{DeriveNumberOfEuRegistrations, EuDetailsQuery}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.CannotAddCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CannotAddCountryController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            cc: AuthenticatedControllerComponents,
                                            view: CannotAddCountryView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)) {
    implicit request =>

      Ok(view(mode, countryIndex))
  }

  def onSubmit(mode: Mode, countryIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.remove(EuDetailsQuery(countryIndex)))
        _ <- cc.sessionRepository.set(updatedAnswers)
      } yield {
        determineRedirect(mode)
      }
  }

  private def determineRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Result = {
    request.userAnswers.get(DeriveNumberOfEuRegistrations) match {
      case Some(n) if n > 1 =>
        Redirect(controllers.euDetails.routes.AddEuDetailsController.onPageLoad(mode).url)
      case _ =>
        Redirect(controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(mode).url)
    }
  }

}
