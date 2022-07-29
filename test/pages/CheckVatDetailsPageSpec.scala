/*
 * Copyright 2022 HM Revenue & Customs
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

package pages

import base.SpecBase
import controllers.routes
import models.CheckVatDetails.{DetailsIncorrect, WrongAccount, Yes}
import models.{CheckVatDetails, NormalMode}
import pages.behaviours.PageBehaviours

class CheckVatDetailsPageSpec extends SpecBase with PageBehaviours {

  "CheckVatDetailsPage" - {

    beRetrievable[CheckVatDetails](CheckVatDetailsPage)

    beSettable[CheckVatDetails](CheckVatDetailsPage)

    beRemovable[CheckVatDetails](CheckVatDetailsPage)

    "must navigate in Normal mode" - {

      "when the user answers Yes" - {

        "when we have VAT details" - {

          "to wherever the Has Trading Name page would navigate to" in {

              val answers = basicUserAnswersWithVatInfo.set(CheckVatDetailsPage, Yes).success.value

              CheckVatDetailsPage.navigate(NormalMode, answers)
                .mustEqual(controllers.routes.HasTradingNameController.onPageLoad(NormalMode))
            }

        }

        "when we don't have VAT details" - {

          "to Journey Recovery" in {

            val answers = emptyUserAnswers.set(CheckVatDetailsPage, Yes).success.value

            CheckVatDetailsPage.navigate(NormalMode, answers)
              .mustEqual(routes.JourneyRecoveryController.onPageLoad())
          }
        }
      }

      "when the user answers Wrong Account" - {

        "to Use Other Account" in {

          val answers = emptyUserAnswers.set(CheckVatDetailsPage, WrongAccount).success.value

          CheckVatDetailsPage.navigate(NormalMode, answers)
            .mustEqual(routes.UseOtherAccountController.onPageLoad())
        }
      }

      "when the user answers Details Incorrect" - {

        "to Update VAT Details" in {

          val answers = emptyUserAnswers.set(CheckVatDetailsPage, DetailsIncorrect).success.value

          CheckVatDetailsPage.navigate(NormalMode, answers)
            .mustEqual(routes.UpdateVatDetailsController.onPageLoad())
        }
      }

      "when the user answers are empty" - {

        "to Update VAT Details" in {
          CheckVatDetailsPage.navigate(NormalMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }
  }
}