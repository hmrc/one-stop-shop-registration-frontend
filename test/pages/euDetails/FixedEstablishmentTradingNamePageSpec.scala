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
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.euDetails

class FixedEstablishmentTradingNamePageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "FixedEstablishmentTradingNamePage" - {

    beRetrievable[String](FixedEstablishmentTradingNamePage(index))

    beSettable[String](euDetails.FixedEstablishmentTradingNamePage(index))

    beRemovable[String](euDetails.FixedEstablishmentTradingNamePage(index))

    "must navigate in Normal mode" - {

      "to Fixed Establishment Address" in {

        FixedEstablishmentTradingNamePage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "when Fixed Establishment Address has not been answered" - {

        "to Fixed Establishment Address" in {

          FixedEstablishmentTradingNamePage(index).navigate(CheckMode, emptyUserAnswers)
            .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(CheckMode, index))
        }
      }

      "when Fixed Establishment Address has already been answered" - {

        "to wherever Fixed Establishment Address would navigate to" in {

          val address = arbitrary[InternationalAddress].sample.value
          val answers = emptyUserAnswers.set(FixedEstablishmentAddressPage(index), address).success.value

          FixedEstablishmentTradingNamePage(index).navigate(CheckMode, answers)
            .mustEqual(FixedEstablishmentAddressPage(index).navigate(CheckMode, answers))
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when Fixed Establishment Address has not been answered" - {

        "to Fixed Establishment Address" in {

          FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(CheckLoopMode, index))
        }
      }

      "when Fixed Establishment Address has already been answered" - {

        "to wherever Fixed Establishment Address would navigate to" in {

          val address = arbitrary[InternationalAddress].sample.value
          val answers = emptyUserAnswers.set(FixedEstablishmentAddressPage(index), address).success.value

          FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, answers)
            .mustEqual(FixedEstablishmentAddressPage(index).navigate(CheckLoopMode, answers))
        }
      }
    }

    "must navigate in Amend mode" - {

      "to Fixed Establishment Address" in {

        FixedEstablishmentTradingNamePage(index).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(AmendMode, index))
      }
    }

    "must navigate in Amend Loop mode" - {

      "when Fixed Establishment Address has not been answered" - {

        "to Fixed Establishment Address" in {

          FixedEstablishmentTradingNamePage(index).navigate(AmendLoopMode, emptyUserAnswers)
            .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(AmendLoopMode, index))
        }
      }

      "when Fixed Establishment Address has already been answered" - {

        "to wherever Fixed Establishment Address would navigate to" in {

          val address = arbitrary[InternationalAddress].sample.value
          val answers = emptyUserAnswers.set(FixedEstablishmentAddressPage(index), address).success.value

          FixedEstablishmentTradingNamePage(index).navigate(AmendLoopMode, answers)
            .mustEqual(FixedEstablishmentAddressPage(index).navigate(AmendLoopMode, answers))
        }
      }
    }

    "must navigate in Rejoin mode" - {

      "to Fixed Establishment Address" in {

        FixedEstablishmentTradingNamePage(index).navigate(RejoinMode, emptyUserAnswers)
          .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(RejoinMode, index))
      }
    }

    "must navigate in Rejoin Loop mode" - {

      "when Fixed Establishment Address has not been answered" - {

        "to Fixed Establishment Address" in {

          FixedEstablishmentTradingNamePage(index).navigate(RejoinLoopMode, emptyUserAnswers)
            .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(RejoinLoopMode, index))
        }
      }

      "when Fixed Establishment Address has already been answered" - {

        "to wherever Fixed Establishment Address would navigate to" in {

          val address = arbitrary[InternationalAddress].sample.value
          val answers = emptyUserAnswers.set(FixedEstablishmentAddressPage(index), address).success.value

          FixedEstablishmentTradingNamePage(index).navigate(RejoinLoopMode, answers)
            .mustEqual(FixedEstablishmentAddressPage(index).navigate(RejoinLoopMode, answers))
        }
      }
    }
  }
}
