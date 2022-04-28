/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.actions.AuthenticatedControllerComponents
import models.euDetails.EuOptionalDetails
import models.requests.AuthenticatedDataRequest
import models.{Country, Index, Mode}
import pages.euDetails
import pages.euDetails.CheckEuDetailsAnswersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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
                                               ) extends FrontendBaseController with CompletionChecks with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
        country =>

          val list = SummaryListViewModel(
            rows = Seq(
              VatRegisteredSummary.row(request.userAnswers, index, mode),
              EuVatNumberSummary.row(request.userAnswers, index, mode),
              EuTaxReferenceSummary.row(request.userAnswers, index, mode),
              HasFixedEstablishmentSummary.row(request.userAnswers, index, mode),
              FixedEstablishmentTradingNameSummary.row(request.userAnswers, index, mode),
              FixedEstablishmentAddressSummary.row(request.userAnswers, index, mode),
              EuSendGoodsSummary.row(request.userAnswers, index, mode),
              EuSendGoodsTradingNameSummary.row(request.userAnswers, index, mode)
            ).flatten
          )

          Future.successful(withCompleteDataModel[EuOptionalDetails](
            index,
            data = getIncompleteEuDetails _,
            onFailure = (incomplete: Option[EuOptionalDetails]) => {
              Ok(view(list, mode, index, country, incomplete.isDefined))
            }) {
            Ok(view(list, mode, index, country))
          })
      }
  }

  def onSubmit(mode: Mode, index: Index, incompletePromptShown: Boolean): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>
      val incomplete = getIncompleteEuDetails(index)
      if(incomplete.isEmpty) {
        Redirect(CheckEuDetailsAnswersPage.navigate(mode, request.userAnswers))
      } else {
        if(!incompletePromptShown) {
          Redirect(routes.CheckEuDetailsAnswersController.onPageLoad(mode, index))
        } else{
          Redirect(routes.EuCountryController.onPageLoad(mode, index))
        }
      }
  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(euDetails.EuCountryPage(index)).map {
      country =>
        block(country)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
