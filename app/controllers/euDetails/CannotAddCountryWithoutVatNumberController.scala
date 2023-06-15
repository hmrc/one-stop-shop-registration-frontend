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

package controllers.euDetails

import controllers.GetCountry
import controllers.actions._
import models.{Index, Mode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{DeriveNumberOfEuRegistrations, EuDetailsQuery}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.CannotAddCountryWithoutVatNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CannotAddCountryWithoutVatNumberController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         view: CannotAddCountryWithoutVatNumberView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with GetCountry {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCountry(countryIndex) {

        country =>

        Future.successful(Ok(view(mode, countryIndex, country)))
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.remove(EuDetailsQuery(countryIndex)))
        _ <- cc.sessionRepository.set(updatedAnswers)
      } yield {
        determineRedirect(mode, updatedAnswers)
      }
  }


  private def determineRedirect(mode: Mode, updatedAnswers: UserAnswers): Result = {
    updatedAnswers.get(DeriveNumberOfEuRegistrations) match {
      case Some(n) if n > 0 =>
        Redirect(controllers.euDetails.routes.AddEuDetailsController.onPageLoad(mode).url)
      case _ =>
        Redirect(controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(mode).url)
    }
  }
}