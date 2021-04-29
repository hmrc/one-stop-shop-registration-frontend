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

package navigation

import base.SpecBase
import controllers.routes
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  val index: Index = Index(0)

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from Registered Company Name to Has Trading Name" in {

        navigator.nextPage(RegisteredCompanyNamePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
      }

      "must go from Has Trading Name" - {

        "to Trading Name when the user answers true" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, true).success.value

          navigator.nextPage(HasTradingNamePage, NormalMode, answers)
            .mustBe(routes.TradingNameController.onPageLoad(NormalMode))
        }

        "to Part of VAT Group when the user answers false" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, false).success.value

          navigator.nextPage(HasTradingNamePage, NormalMode, answers)
            .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
        }
      }

      "must go from Trading Name to Part of VAT Group" in {

        navigator.nextPage(TradingNamePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
      }

      "must go from Part of VAT Group to UK VAT Number" in {

        navigator.nextPage(PartOfVatGroupPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.UkVatNumberController.onPageLoad(NormalMode))
      }

      "must go from UK VAT Number to UK VAT Effective Date" in {

        navigator.nextPage(UkVatNumberPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.UkVatEffectiveDateController.onPageLoad(NormalMode))
      }

      "must go from UK VAT Effective Date to UK VAT Registered Postcode" in {

        navigator.nextPage(UkVatEffectiveDatePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.UkVatRegisteredPostcodeController.onPageLoad(NormalMode))
      }

      "must go from UK VAT Registered Postcode to VAT Registered in EU" in {

        navigator.nextPage(UkVatRegisteredPostcodePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.VatRegisteredInEuController.onPageLoad(NormalMode))
      }

      "must go from VAT Registered in EU" - {

        "to VAT Registered EU Member State when the user answers true" in {

          val answers = emptyUserAnswers.set(VatRegisteredInEuPage, true).success.value

          navigator.nextPage(VatRegisteredInEuPage, NormalMode, answers)
            .mustBe(routes.VatRegisteredEuMemberStateController.onPageLoad(NormalMode, index))
        }

        "TO DO - User answers FALSE" in {

          val answers = emptyUserAnswers.set(VatRegisteredInEuPage, false).success.value

          navigator.nextPage(VatRegisteredInEuPage, NormalMode, answers)
            .mustBe(routes.RegisteredCompanyNameController.onPageLoad(NormalMode))
        }
      }

      "must got to EU VAT Number from VAT Registered EU Member State" in {

        navigator.nextPage(VatRegisteredEuMemberStatePage(index), NormalMode, emptyUserAnswers)
          .mustBe(routes.EuVatNumberController.onPageLoad(NormalMode, index))
      }

      "must got to Add Additional EU VAT Details from EU VAT Number" in {

        navigator.nextPage(EuVatNumberPage(index), NormalMode, emptyUserAnswers)
          .mustBe(routes.AddAdditionalEuVatDetailsController.onPageLoad(NormalMode))
      }

      "must go from Add Additional EU VAT Details" - {

        "to VAT Registered EU Member State when the user answers true" in {

          val answers = emptyUserAnswers
            .set(AddAdditionalEuVatDetailsPage, true).success.value
            .set(VatRegisteredEuMemberStatePage(Index(0)), "France").success.value
            .set(EuVatNumberPage(Index(0)), "FR123456789").success.value

          navigator.nextPage(AddAdditionalEuVatDetailsPage, NormalMode, answers)
            .mustBe(routes.VatRegisteredEuMemberStateController.onPageLoad(NormalMode, Index(1)))
        }

        "TO DO - User answers FALSE" in {

          val answers = emptyUserAnswers.set(AddAdditionalEuVatDetailsPage, false).success.value

          navigator.nextPage(AddAdditionalEuVatDetailsPage, NormalMode, answers)
            .mustBe(routes.RegisteredCompanyNameController.onPageLoad(NormalMode))
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
