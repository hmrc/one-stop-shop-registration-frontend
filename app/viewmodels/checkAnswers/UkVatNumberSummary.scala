package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.UkVatNumberPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UkVatNumberSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(UkVatNumberPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "ukVatNumber.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.UkVatNumberController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("ukVatNumber.change.hidden"))
          )
        )
    }
}
