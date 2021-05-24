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

import controllers.actions._
import forms.AddAdditionalEuVatDetailsFormProvider

import javax.inject.Inject
import models.{Country, Mode}
import models.requests.DataRequest
import navigation.Navigator
import pages.AddAdditionalEuVatDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.DeriveNumberOfEuVatRegisteredCountries
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.EuVatDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.AddAdditionalEuVatDetailsView

import scala.concurrent.{ExecutionContext, Future}

class AddAdditionalEuVatDetailsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddAdditionalEuVatDetailsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AddAdditionalEuVatDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getNumberOfEuCountries {
        number =>

          val canAddCountries = number < Country.euCountries.size
          Future.successful(Ok(view(form, mode, EuVatDetailsSummary.addToListRows(request.userAnswers), canAddCountries)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getNumberOfEuCountries {
        number =>

          val canAddCountries = number < Country.euCountries.size

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, mode, EuVatDetailsSummary.addToListRows(request.userAnswers), canAddCountries))
              ),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAdditionalEuVatDetailsPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AddAdditionalEuVatDetailsPage, mode, updatedAnswers))
          )
      }
  }

  private def getNumberOfEuCountries(block: Int => Future[Result])
                                    (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfEuVatRegisteredCountries).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
