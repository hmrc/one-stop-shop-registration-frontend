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

package controllers.euDetails

import controllers.actions._
import forms.euDetails.AddEuDetailsFormProvider
import models.{Country, Mode}
import models.requests.DataRequest
import navigation.Navigator
import pages.euDetails.AddEuDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.DeriveNumberOfEuVatRegisteredCountries
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.euDetails.EuDetailsSummary
import views.html.euDetails.AddEuDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddEuDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        navigator: Navigator,
                                        formProvider: AddEuDetailsFormProvider,
                                        view: AddEuDetailsView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc
  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getNumberOfEuCountries {
        number =>

          val canAddCountries = number < Country.euCountries.size
          Future.successful(Ok(view(form, mode, EuDetailsSummary.addToListRows(request.userAnswers), canAddCountries)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getNumberOfEuCountries {
        number =>
          val canAddCountries = number < Country.euCountries.size
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, mode, EuDetailsSummary.addToListRows(request.userAnswers), canAddCountries))
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddEuDetailsPage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AddEuDetailsPage, mode, updatedAnswers))
          )
      }
  }

  private def getNumberOfEuCountries(block: Int => Future[Result])
                                    (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfEuVatRegisteredCountries).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
