/*
 * Copyright 2021 HM Revenue & Customs
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

import config.Constants
import controllers.actions._
import forms.AddTradingNameFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.AddTradingNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.DeriveNumberOfTradingNames
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.TradingNameSummary
import views.html.AddTradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddTradingNameController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AddTradingNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddTradingNameView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getNumberOfTradingNames {
        number =>
          val canAddTradingNames = number < Constants.maxTradingNames
          Future.successful(Ok(view(form, mode, TradingNameSummary.addToListRows(request.userAnswers), canAddTradingNames)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getNumberOfTradingNames {
        number =>
          val canAddTradingNames = number < Constants.maxTradingNames

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, mode, TradingNameSummary.addToListRows(request.userAnswers), canAddTradingNames))
              ),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddTradingNamePage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AddTradingNamePage, mode, updatedAnswers))
          )
      }
  }

  private def getNumberOfTradingNames(block: Int => Future[Result])
                                     (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfTradingNames).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
