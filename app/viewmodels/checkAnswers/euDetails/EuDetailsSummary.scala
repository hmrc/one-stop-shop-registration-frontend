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

package viewmodels.checkAnswers.euDetails

import controllers.euDetails.routes
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Country, Index, Mode, NormalMode, RejoinLoopMode, RejoinMode, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.AllEuOptionalDetailsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.ListItemWrapper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object EuDetailsSummary {

  def addToListRows(answers: UserAnswers, currentMode: Mode): Seq[ListItemWrapper] = {

    val changeLinkMode = currentMode match {
      case NormalMode => NormalMode
      case CheckMode => CheckMode
      case AmendMode => AmendMode
      case RejoinMode => RejoinMode
      case CheckLoopMode => throw new IllegalArgumentException("EuDetailsSummary.addToListRows cannot be rendered in Check Loop Mode")
      case AmendLoopMode => throw new IllegalArgumentException("EuDetailsSummary.addToListRows cannot be rendered in Amend Loop Mode")
      case RejoinLoopMode => throw new IllegalArgumentException("EuDetailsSummary.addToListRows cannot be rendered in Rejoin Loop Mode")
    }

    answers.get(AllEuOptionalDetailsQuery).getOrElse(List.empty).zipWithIndex.map {
      case (details, index) =>
        ListItemWrapper(
          ListItem(
          name = HtmlFormat.escape(details.euCountry.name).toString,
          changeUrl = routes.CheckEuDetailsAnswersController.onPageLoad(changeLinkMode, Index(index)).url,
          removeUrl = routes.DeleteEuDetailsController.onPageLoad(currentMode, Index(index)).url
        ),
          removeButtonEnabled = true
        )
    }
  }

  def checkAnswersRow(answers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllEuOptionalDetailsQuery).map {
      euVatDetails =>

        val value = euVatDetails.map {
          details =>
            HtmlFormat.escape(details.euCountry.name)
        }.mkString("<br/>")

        SummaryListRowViewModel(
          key = "euVatDetails.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AddEuDetailsController.onPageLoad(mode).url)
              .withVisuallyHiddenText(messages("euVatDetails.change.hidden"))
          )
        )
    }

  def countryAndVatNumberList(answers: UserAnswers, currentMode: Mode)(implicit messages: Messages) = {
    val changeLinkMode = currentMode match {
      case NormalMode => NormalMode
      case CheckMode => CheckMode
      case AmendMode => AmendMode
      case CheckLoopMode => throw new IllegalArgumentException("EuDetailsSummary.addToListRows cannot be rendered in Check Loop Mode")
      case AmendLoopMode => throw new IllegalArgumentException("EuDetailsSummary.addToListRows cannot be rendered in Amend Loop Mode")
      case RejoinMode     => RejoinMode
      case RejoinLoopMode => throw new IllegalArgumentException("EuDetailsSummary.addToListRows cannot be rendered in Rejoin Loop Mode")
    }

      SummaryList(
        answers.get(AllEuOptionalDetailsQuery).getOrElse(List.empty).zipWithIndex.map {
        case (euVatDetails, index) =>

          val value = euVatDetails.euVatNumber.getOrElse("") + euVatDetails.euTaxReference.getOrElse("")

          SummaryListRowViewModel(
            key = euVatDetails.euCountry.name,
            value = ValueViewModel(HtmlContent(value)),
            actions = Seq(
              ActionItemViewModel("site.change", routes.CheckEuDetailsAnswersController.onPageLoad(changeLinkMode, Index(index)).url)
                .withVisuallyHiddenText(messages("change.euVatNumber.hidden", euVatDetails.euCountry.name)),
              ActionItemViewModel("site.remove", routes.DeleteEuDetailsController.onPageLoad(currentMode, Index(index)).url)
                .withVisuallyHiddenText(messages("site.remove.hidden", euVatDetails.euCountry.name))
            ),
            actionClasses = "govuk-!-width-one-third"
          )
      }
      )
  }

  def amendedAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllEuOptionalDetailsQuery).map {
      euVatDetails =>

        val value = euVatDetails.map {
          details =>
            HtmlFormat.escape(details.euCountry.name)
        }.mkString("<br/>")

        SummaryListRowViewModel(
          key = KeyViewModel("euVatDetails.checkYourAnswersLabel").withCssClass("govuk-!-width-one-half"),
          value = ValueViewModel(HtmlContent(value))
        )
    }

  def removedAnswersRow(removedEuDetails: Seq[Country])(implicit messages: Messages): Option[SummaryListRow] =

    if (removedEuDetails.nonEmpty) {
        val value = removedEuDetails.map {
          details =>
            HtmlFormat.escape(details.name)
        }.mkString("<br/>")

        Some(
          SummaryListRowViewModel(
            key = KeyViewModel("euVatDetails.checkYourAnswersLabel.removed").withCssClass("govuk-!-width-one-half"),
            value = ValueViewModel(HtmlContent(value))
          )
        )
    } else {
      None
    }

  def changedAnswersRow(changedEuDetails: Seq[Country])(implicit messages: Messages): Option[SummaryListRow] =

    if (changedEuDetails.nonEmpty) {
      val value = changedEuDetails.map {
        details =>
          HtmlFormat.escape(details.name)
      }.mkString("<br/>")

      Some(
        SummaryListRowViewModel(
          key = KeyViewModel("euVatDetails.checkYourAnswersLabel.changed").withCssClass("govuk-!-width-one-half"),
          value = ValueViewModel(HtmlContent(value))
        )
      )
    } else {
      None
    }
}
