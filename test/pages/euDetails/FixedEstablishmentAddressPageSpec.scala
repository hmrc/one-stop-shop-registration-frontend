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

package pages.euDetails

import base.SpecBase
import controllers.euDetails.routes as euRoutes
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, InternationalAddress, NormalMode, RejoinLoopMode, RejoinMode}
import pages.behaviours.PageBehaviours
import pages.euDetails

class FixedEstablishmentAddressPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "FixedEstablishmentAddressPage" - {

    beRetrievable[InternationalAddress](FixedEstablishmentAddressPage(index))

    beSettable[InternationalAddress](euDetails.FixedEstablishmentAddressPage(index))

    beRemovable[InternationalAddress](euDetails.FixedEstablishmentAddressPage(index))

    "must navigate in Normal mode" - {

      "to Check Eu Details Answers in Normal mode" in {

        FixedEstablishmentAddressPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "to Check Eu Details Answers in Check mode" in {

        FixedEstablishmentAddressPage(index).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, index))
      }
    }

    "must navigate in Check Loop mode" - {

      "to Check Eu Details Answers in Normal mode" in {

        FixedEstablishmentAddressPage(index).navigate(CheckLoopMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Amend mode" - {

      "to Check Eu Details Answers in Check mode" in {

        FixedEstablishmentAddressPage(index).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(AmendMode, index))
      }
    }

    "must navigate in Amend Loop mode" - {

      "to Check Eu Details Answers in Amend mode" in {

        FixedEstablishmentAddressPage(index).navigate(AmendLoopMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(AmendMode, index))
      }
    }

    "must navigate in Rejoin mode" - {

      "to Check Eu Details Answers in Rejoin mode" in {

        FixedEstablishmentAddressPage(index).navigate(RejoinMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(RejoinMode, index))
      }
    }

    "must navigate in Rejoin Loop mode" - {

      "to Check Eu Details Answers in Rejoin mode" in {

        FixedEstablishmentAddressPage(index).navigate(RejoinLoopMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(RejoinMode, index))
      }
    }
  }
}
