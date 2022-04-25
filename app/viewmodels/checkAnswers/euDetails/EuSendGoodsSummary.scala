package viewmodels.checkAnswers.euDetails

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.EuSendGoodsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object EuSendGoodsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(EuSendGoodsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "euSendGoods.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.EuSendGoodsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("euSendGoods.change.hidden"))
          )
        )
    }
}
