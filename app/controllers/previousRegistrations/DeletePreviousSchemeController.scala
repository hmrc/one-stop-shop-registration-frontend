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
import forms.previousRegistrations.DeletePreviousSchemeFormProvider
import models.requests.AuthenticatedDataRequest
import models.{Country, Index, Mode}
import pages.previousRegistrations.{DeletePreviousSchemePage, PreviousEuCountryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration.{DeriveNumberOfPreviousSchemes, PreviousSchemeForCountryQuery}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.previousRegistrations.{DeletePreviousSchemeSummary, PreviousIntermediaryNumberSummary, PreviousSchemeNumberSummary}
import viewmodels.govuk.summarylist._
import views.html.previousRegistrations.DeletePreviousSchemeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeletePreviousSchemeController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                cc: AuthenticatedControllerComponents,
                                                formProvider: DeletePreviousSchemeFormProvider,
                                                view: DeletePreviousSchemeView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      val isLastPreviousScheme = request.userAnswers.get(DeriveNumberOfPreviousSchemes(countryIndex)).get == 1

      getCountry(countryIndex) {
        country =>

            val list =
              SummaryListViewModel(
                rows = Seq(
                  DeletePreviousSchemeSummary.row(request.userAnswers, countryIndex, schemeIndex),
                  PreviousSchemeNumberSummary.row(request.userAnswers, countryIndex, schemeIndex),
                  PreviousIntermediaryNumberSummary.row(request.userAnswers, countryIndex, schemeIndex)
                ).flatten
              )

            val form = formProvider(country)

            val preparedForm = request.userAnswers.get(DeletePreviousSchemePage(countryIndex)) match {
              case None => form
              case Some(value) => form.fill(value)
            }

            Future.successful(Ok(view(preparedForm, mode, countryIndex, schemeIndex, country, list, isLastPreviousScheme)))
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      val isLastPreviousScheme = request.userAnswers.get(DeriveNumberOfPreviousSchemes(countryIndex)).get == 1

      getCountry(countryIndex) {
        country =>

          val list =
            SummaryListViewModel(
              rows = Seq(
                DeletePreviousSchemeSummary.row(request.userAnswers, countryIndex, schemeIndex),
                PreviousSchemeNumberSummary.row(request.userAnswers, countryIndex, schemeIndex),
                PreviousIntermediaryNumberSummary.row(request.userAnswers, countryIndex, schemeIndex)
              ).flatten
            )

            val form = formProvider(country)

            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, countryIndex, schemeIndex, country, list, isLastPreviousScheme))),

              value =>
                if (value) {
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.remove(PreviousSchemeForCountryQuery(countryIndex, schemeIndex)))
                    _ <- cc.sessionRepository.set(updatedAnswers)
                  } yield Redirect(DeletePreviousSchemePage(countryIndex).navigate(mode, updatedAnswers))
                } else {
                  Future.successful(Redirect(DeletePreviousSchemePage(countryIndex).navigate(mode, request.userAnswers)))
                }
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

