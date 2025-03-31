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
import forms.CheckVatDetailsFormProvider
import models.CheckVatDetails.Yes
import models.requests.AuthenticatedDataRequest
import models.{CheckVatDetails, NormalMode, UserAnswers}
import pages.{CheckVatDetailsPage, HasTradingNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.AllTradingNames
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import viewmodels.CheckVatDetailsViewModel
import views.html.CheckVatDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CheckVatDetailsController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           cc: AuthenticatedControllerComponents,
                                           formProvider: CheckVatDetailsFormProvider,
                                           view: CheckVatDetailsView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      request.userAnswers.vatInfo match {
        case Some(vatInfo) =>
          val preparedForm = request.userAnswers.get(CheckVatDetailsPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          val viewModel = CheckVatDetailsViewModel(request.vrn, vatInfo)
          Ok(view(preparedForm, viewModel))

        case None =>
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      request.userAnswers.vatInfo match {
        case Some(vatInfo) =>
          form.bindFromRequest().fold(
            formWithErrors => {
              val viewModel = CheckVatDetailsViewModel(request.vrn, vatInfo)
              BadRequest(view(formWithErrors, viewModel)).toFuture
            },

            value =>
              for {
                answersWithIossTradingNamesCheck <- Future.fromTry(iossTradingNamesAnswersTry(value, request))
                updatedAnswers <- Future.fromTry(answersWithIossTradingNamesCheck.set(CheckVatDetailsPage, value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(CheckVatDetailsPage.navigate(NormalMode, updatedAnswers))
          )

        case None =>
          Redirect(routes.JourneyRecoveryController.onPageLoad()).toFuture
      }
  }

  private def iossTradingNamesAnswersTry(value: CheckVatDetails, request: AuthenticatedDataRequest[AnyContent]): Try[UserAnswers] = {
    if (value == Yes) {
      request.latestIossRegistration match {
        case Some(iossRegistration) if iossRegistration.tradingNames.nonEmpty =>
          for {
            answers <- request.userAnswers.set(HasTradingNamePage, true)
            updatedAnswers <- answers.set(AllTradingNames, iossRegistration.tradingNames.map(_.tradingName).toList)
          } yield updatedAnswers

        case _ =>
          Try(request.userAnswers)
      }
    } else {
      Try(request.userAnswers)
    }
  }
}
