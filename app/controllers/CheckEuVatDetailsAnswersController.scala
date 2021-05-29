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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{Index, NormalMode}
import navigation.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckEuVatDetailsAnswersView

import javax.inject.Inject

class CheckEuVatDetailsAnswersController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: CheckEuVatDetailsAnswersView
                                                  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          VatRegisteredEuMemberStateSummary.row(request.userAnswers, index),
          EuVatNumberSummary.row(request.userAnswers, index),
          HasFixedEstablishmentSummary.row(request.userAnswers, index),
          FixedEstablishmentTradingNameSummary.row(request.userAnswers, index),
          FixedEstablishmentAddressSummary.row(request.userAnswers, index)
        ).flatten
      )

      Ok(view(list, index))
  }

  def onSubmit(index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(routes.AddAdditionalEuVatDetailsController.onPageLoad(NormalMode))
  }
}
