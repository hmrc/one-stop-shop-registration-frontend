package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.UkVatRegisteredPostcodePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UkVatRegisteredPostcodeSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(UkVatRegisteredPostcodePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "ukVatRegisteredPostcode.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.UkVatRegisteredPostcodeController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("ukVatRegisteredPostcode.change.hidden"))
          )
        )
    }
}
