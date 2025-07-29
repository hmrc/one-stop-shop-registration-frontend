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

package controllers.previousRegistrations

import config.Constants
import controllers.GetCountry
import controllers.actions.AuthenticatedControllerComponents
import forms.previousRegistrations.CheckPreviousSchemeAnswersFormProvider
import models.{AmendMode, Index, Mode, RejoinMode}
import pages.previousRegistrations.CheckPreviousSchemeAnswersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.previousRegistration.AllPreviousSchemesForCountryWithOptionalVatNumberQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckExistingRegistrations.getExistingRegistrationSchemes
import utils.CheckJourneyRecovery.determineJourneyRecovery
import utils.CompletionChecks
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.previousRegistrations._
import viewmodels.govuk.summarylist._
import views.html.previousRegistrations.CheckPreviousSchemeAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckPreviousSchemeAnswersController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      cc: AuthenticatedControllerComponents,
                                                      formProvider: CheckPreviousSchemeAnswersFormProvider,
                                                      view: CheckPreviousSchemeAnswersView
                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with CompletionChecks with I18nSupport with GetCountry {


  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getPreviousCountry(mode, index) {
        country =>
          request.userAnswers.get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(index)).map { previousSchemes =>

            val canAddScheme = previousSchemes.size < Constants.maxSchemes

            val existingSchemes = if (mode == AmendMode || mode == RejoinMode) getExistingRegistrationSchemes(country) else Seq.empty

            val lists = previousSchemes.zipWithIndex.map { case (_, schemeIndex) =>
              SummaryListViewModel(
                rows = Seq(
                 if (mode == AmendMode || mode == RejoinMode) {
                    PreviousSchemeSummary.row(request.userAnswers, index, Index(schemeIndex), country, existingSchemes, mode)
                  } else {
                    PreviousSchemeSummary.row(request.userAnswers, index, Index(schemeIndex), country, Seq.empty, mode)
                  },
                  PreviousSchemeNumberSummary.row(request.userAnswers, index, Index(schemeIndex))
                ).flatten
              )
            }

            val form = formProvider(country)

            Future.successful(Ok(view(form, mode, lists, index, country, canAddScheme)))

          }.getOrElse(Redirect(determineJourneyRecovery(Some(mode))).toFuture)

      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      getPreviousCountry(mode, index) { country =>

        request.userAnswers.get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(index)).map { previousSchemes =>

          val canAddScheme = previousSchemes.size < Constants.maxSchemes

          val existingSchemes = if (mode == AmendMode || mode == RejoinMode) getExistingRegistrationSchemes(country) else Seq.empty

          val lists = previousSchemes.zipWithIndex.map { case (_, schemeIndex) =>
            SummaryListViewModel(
              rows = Seq(
                if (mode == AmendMode || mode == RejoinMode) {
                  PreviousSchemeSummary.row(request.userAnswers, index, Index(schemeIndex), country, existingSchemes, mode)
                } else {
                  PreviousSchemeSummary.row(request.userAnswers, index, Index(schemeIndex), country,  Seq.empty, mode)
                },
                PreviousSchemeNumberSummary.row(request.userAnswers, index, Index(schemeIndex))
              ).flatten
            )
          }

          val form = formProvider(country)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, lists, index, country, canAddScheme))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(CheckPreviousSchemeAnswersPage(index), value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(CheckPreviousSchemeAnswersPage(index).navigate(mode, updatedAnswers))
          )
        }.getOrElse(Redirect(determineJourneyRecovery(Some(mode))).toFuture)
      }
  }

}

