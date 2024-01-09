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

import controllers.GetCountry
import controllers.actions.AuthenticatedControllerComponents
import models.euDetails.EuOptionalDetails
import models.{Index, Mode}
import pages.euDetails.CheckEuDetailsAnswersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.EuDetailsCompletionChecks.getIncompleteEuDetails
import utils.CompletionChecks
import viewmodels.checkAnswers.euDetails._
import viewmodels.govuk.summarylist._
import views.html.euDetails.CheckEuDetailsAnswersView

import javax.inject.Inject
import scala.concurrent.Future

class CheckEuDetailsAnswersController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 cc: AuthenticatedControllerComponents,
                                                 view: CheckEuDetailsAnswersView
                                               ) extends FrontendBaseController with CompletionChecks with I18nSupport with GetCountry {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCountry(mode, countryIndex) {
        country =>

          val list = SummaryListViewModel(
            rows = Seq(
              SellsGoodsToEUConsumersSummary.row(request.userAnswers, countryIndex, mode),
              SellsGoodsToEUConsumerMethodSummary.row(request.userAnswers, countryIndex, mode),
              RegistrationTypeSummary.row(request.userAnswers, countryIndex, mode),
              VatRegisteredSummary.row(request.userAnswers, countryIndex, mode),
              EuVatNumberSummary.row(request.userAnswers, countryIndex, mode),
              EuTaxReferenceSummary.row(request.userAnswers, countryIndex, mode),
              FixedEstablishmentTradingNameSummary.row(request.userAnswers, countryIndex, mode),
              FixedEstablishmentAddressSummary.row(request.userAnswers, countryIndex, mode),
              EuSendGoodsTradingNameSummary.row(request.userAnswers, countryIndex, mode),
              EuSendGoodsAddressSummary.row(request.userAnswers, countryIndex, mode)
            ).flatten
          )

          Future.successful(withCompleteDataModel[EuOptionalDetails](
            countryIndex,
            data = getIncompleteEuDetails _,
            onFailure = (incomplete: Option[EuOptionalDetails]) => {
              Ok(view(list, mode, countryIndex, country, incomplete.isDefined))
            }) {
            Ok(view(list, mode, countryIndex, country))
          })
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index, incompletePromptShown: Boolean): Action[AnyContent] = cc.authAndGetData(Some(mode)) {
    implicit request =>
      val incomplete = getIncompleteEuDetails(countryIndex)
      if (incomplete.isEmpty) {
        Redirect(CheckEuDetailsAnswersPage.navigate(mode, request.userAnswers))
      } else {
        if (!incompletePromptShown) {
          Redirect(routes.CheckEuDetailsAnswersController.onPageLoad(mode, countryIndex))
        } else {
          incompleteCountryEuDetailsRedirect(mode).map {
            redirectIncompletePage =>
              redirectIncompletePage
          }.getOrElse(Redirect(routes.EuCountryController.onPageLoad(mode, countryIndex)))
        }
      }
  }

}

