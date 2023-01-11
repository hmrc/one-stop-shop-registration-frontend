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

package controllers.previousRegistrations

import controllers.actions._
import forms.previousRegistrations.PreviousSchemeTypeFormProvider
import models.{Country, Index, Mode}
import models.requests.AuthenticatedDataRequest
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousSchemeTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration.AllPreviousSchemesForCountryWithOptionalVatNumberQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.previousRegistrations.PreviousSchemeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousSchemeController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          cc: AuthenticatedControllerComponents,
                                          formProvider: PreviousSchemeTypeFormProvider,
                                          view: PreviousSchemeView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(countryIndex) {
        country =>

          val form = request.userAnswers.get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(countryIndex)) match {
            case Some(previousSchemesDetails) =>

              val previousSchemes = previousSchemesDetails.flatMap(_.previousScheme)
              formProvider(country.name, previousSchemes)

            case None =>
              formProvider(country.name, Seq.empty)
          }

          val preparedForm = request.userAnswers.get(PreviousSchemeTypePage(countryIndex, schemeIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, countryIndex, schemeIndex)))
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(countryIndex) {
        country =>
          val form = request.userAnswers.get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(countryIndex)) match {
            case Some(previousSchemesDetails) =>

              val previousSchemes = previousSchemesDetails.flatMap(_.previousScheme)
              formProvider(country.name, previousSchemes)

            case None =>
              formProvider(country.name, Seq.empty)
          }

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, countryIndex, schemeIndex))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviousSchemeTypePage(countryIndex, schemeIndex), value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(PreviousSchemeTypePage(countryIndex, schemeIndex).navigate(mode, updatedAnswers))
          )
      }
  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(PreviousEuCountryPage(index)).map {
      country =>
        block(country)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
