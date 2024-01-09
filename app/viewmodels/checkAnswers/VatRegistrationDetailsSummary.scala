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

package viewmodels.checkAnswers

import formats.Format.dateOfRegistrationFormatter
import models.UserAnswers
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object VatRegistrationDetailsSummary {

  def rowBusinessName(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.vatInfo.flatMap {
      answer =>

        answer.organisationName.map { organisationName =>
          val value = HtmlFormat.escape(organisationName).toString

          SummaryListRowViewModel(
            key = "vatRegistrationDetails.checkYourAnswers.organisationName",
            value = ValueViewModel(HtmlContent(value))
          )
        }
    }

  def rowIndividualName(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.vatInfo.flatMap {
      answer =>

        answer.individualName.map { individualName =>
          val value = HtmlFormat.escape(individualName).toString

          SummaryListRowViewModel(
            key = "vatRegistrationDetails.checkYourAnswers.individualName",
            value = ValueViewModel(HtmlContent(value))
          )
        }
    }

  def rowPartOfVatUkGroup(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.vatInfo.map {
      answer =>

        val value = if (answer.partOfVatGroup) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "vatRegistrationDetails.checkYourAnswers.partOfVatGroup",
          value = ValueViewModel(value)
        )
    }

  def rowUkVatRegistrationDate(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.vatInfo.map {
      answer =>

        val value = HtmlFormat.escape(answer.registrationDate.format(dateOfRegistrationFormatter))

        SummaryListRowViewModel(
          key = "vatRegistrationDetails.checkYourAnswers.vatRegistrationDate",
          value = ValueViewModel(HtmlContent(value))
        )
    }

  def rowBusinessAddress(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.vatInfo.map {
      answer =>

        val value = Seq(
          Some(HtmlFormat.escape(answer.address.line1).toString),
          answer.address.line2.map(HtmlFormat.escape),
          answer.address.line3.map(HtmlFormat.escape),
          answer.address.line4.map(HtmlFormat.escape),
          answer.address.line5.map(HtmlFormat.escape),
          answer.address.postCode.map(HtmlFormat.escape),
          Some(HtmlFormat.escape(answer.address.countryCode).toString)
        ).flatten.mkString("<br/>")

        SummaryListRowViewModel(
          key = "vatRegistrationDetails.checkYourAnswers.businessAddress",
          value = ValueViewModel(HtmlContent(value))
        )
    }

}
