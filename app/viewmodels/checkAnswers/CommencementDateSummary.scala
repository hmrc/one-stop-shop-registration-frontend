package viewmodels.checkAnswers

import formats.Format.dateFormatter
import models.UserAnswers
import pages.DateOfFirstSalePage
import play.api.i18n.Messages
import services.StartDateService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import javax.inject.Inject

class CommencementDateSummary @Inject()(startDateService: StartDateService) {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DateOfFirstSalePage).map {
      answer =>

        val startDate = startDateService.startDateBasedOnFirstSale(answer)

        SummaryListRowViewModel(
          key     = "ukVatEffectiveDate.checkYourAnswersLabel",
          value   = ValueViewModel(startDate.format(dateFormatter)),
          actions = Seq.empty
        )
    }
}