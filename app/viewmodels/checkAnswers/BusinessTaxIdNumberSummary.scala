package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.BusinessTaxIdNumberPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessTaxIdNumberSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BusinessTaxIdNumberPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "businessTaxIdNumber.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.BusinessTaxIdNumberController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("businessTaxIdNumber.change.hidden"))
          )
        )
    }
}
