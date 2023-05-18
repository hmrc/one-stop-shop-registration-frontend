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

package viewmodels.checkAnswers.previousRegistrations

import controllers.previousRegistrations.routes
import models.domain.{PreviousRegistration, PreviousRegistrationLegacy, PreviousRegistrationNew}
import models.{AmendMode, Country, Index, Mode, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.previousRegistration.{AllPreviousRegistrationsQuery, AllPreviousRegistrationsWithOptionalVatNumberQuery}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.ListItemWrapper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PreviousRegistrationSummary {

  def addToListRows(answers: UserAnswers, existingPreviousRegistrations: Seq[PreviousRegistration], mode: Mode): Seq[ListItemWrapper] =
    answers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery).getOrElse(List.empty).zipWithIndex.map {
      case (details, index) =>
        if (mode == AmendMode) {

          ListItemWrapper(
            ListItem(
              name = HtmlFormat.escape(details.previousEuCountry.name).toString,
              changeUrl = routes.CheckPreviousSchemeAnswersController.onPageLoad(mode, Index(index)).url,
              removeUrl = routes.DeletePreviousRegistrationController.onPageLoad(mode, Index(index)).url
            ),
            !existingPreviousRegistration(details.previousEuCountry, existingPreviousRegistrations)
          )
        } else {
          ListItemWrapper(
            ListItem(
              name = HtmlFormat.escape(details.previousEuCountry.name).toString,
              changeUrl = routes.CheckPreviousSchemeAnswersController.onPageLoad(mode, Index(index)).url,
              removeUrl = routes.DeletePreviousRegistrationController.onPageLoad(mode, Index(index)).url
            ),
            removeButtonEnabled = true
          )
        }
    }

  private def existingPreviousRegistration(country: Country, existingPreviousRegistration: Seq[PreviousRegistration]): Boolean = {
    existingPreviousRegistration.exists {
      case previousRegistrationNew: PreviousRegistrationNew => previousRegistrationNew.country == country
      case previousRegistrationLegacy: PreviousRegistrationLegacy => previousRegistrationLegacy.country == country
    }
  }

  def checkAnswersRow(answers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllPreviousRegistrationsQuery).map {
      previousRegistrations =>

        val value = previousRegistrations.map {
          details =>
            HtmlFormat.escape(details.previousEuCountry.name)
        }.mkString("<br/>")

        SummaryListRowViewModel(
          key = "previousRegistrations.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          //TODO - Add additional check to see
          actions = Seq(if (mode == AmendMode) {
            ActionItemViewModel("site.add", routes.AddPreviousRegistrationController.onPageLoad(mode).url)
              .withVisuallyHiddenText(messages("previousRegistrations.add.hidden"))
          } else {
            ActionItemViewModel("site.change", routes.AddPreviousRegistrationController.onPageLoad(mode).url)
              .withVisuallyHiddenText(messages("previousRegistrations.change.hidden"))
          }
          )
        )
    }

}
