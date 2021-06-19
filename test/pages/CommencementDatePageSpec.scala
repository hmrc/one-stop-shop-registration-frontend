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

package pages

import base.SpecBase
import controllers.routes
import models.{CheckMode, NormalMode}

class CommencementDatePageSpec extends SpecBase {

  "CommencementDatePage" - {

    "must navigate in Normal mode" - {

      "to Check VAT Details when we have VAT details for the user" in {

        CommencementDatePage.navigate(NormalMode, emptyUserAnswersWithVatInfo)
          .mustEqual(routes.CheckVatDetailsController.onPageLoad())
      }

      "to Check VAT Number when we do not have VAT details for the user" in {

        CommencementDatePage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.CheckVatNumberController.onPageLoad())
      }
    }

    "must navigate in Check mode" - {

      "to Check Your Answers" in {

        CommencementDatePage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }
    }
  }
}