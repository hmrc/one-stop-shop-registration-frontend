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

package controllers

import controllers.actions.*
import forms.BankDetailsFormProvider
import models.requests.AuthenticatedDataRequest
import models.{BankDetails, Mode}
import pages.BankDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BankDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BankDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       formProvider: BankDetailsFormProvider,
                                       view: BankDetailsView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetDataAndCheckVerifyEmail(Some(mode)) {
    implicit request =>

      val preparedForm = request.userAnswers.get(BankDetailsPage) match {
        case None => fillIossBankDetailsForm(request)
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.latestIossRegistration, request.numberOfIossRegistrations))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.latestIossRegistration, request.numberOfIossRegistrations))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BankDetailsPage, value))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(BankDetailsPage.navigate(mode, updatedAnswers))
      )
  }

  private def fillIossBankDetailsForm(request: AuthenticatedDataRequest[_]): Form[BankDetails] = {
    request.latestIossRegistration match {
      case Some(iossEtmpDisplayRegistration) =>
        form.fill(
          BankDetails(
            accountName = iossEtmpDisplayRegistration.bankDetails.accountName,
            bic = iossEtmpDisplayRegistration.bankDetails.bic,
            iban = iossEtmpDisplayRegistration.bankDetails.iban
          )
        )

      case _ => form
    }
  }
}
