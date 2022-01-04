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

package pages.euDetails

import base.SpecBase
import controllers.euDetails.{routes => euRoutes}
import models.{CheckLoopMode, CheckMode, Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.euDetails

class EuVatNumberPageSpec extends SpecBase with PageBehaviours {

  val index: Index = Index(0)

  "EuVatNumberPage" - {

    beRetrievable[String](EuVatNumberPage(index))

    beSettable[String](euDetails.EuVatNumberPage(index))

    beRemovable[String](euDetails.EuVatNumberPage(index))

    "must navigate in Normal mode" - {

      "to Has Fixed Establishment for the same index" in {

        EuVatNumberPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "when Has Fixed Establishment has not been answered" - {

        "to Has Fixed Establishment" in {

          EuVatNumberPage(index).navigate(CheckMode, emptyUserAnswers)
            .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(CheckMode, index))
        }
      }

      "when Has Fixed Establishment has been answered" - {

        "to wherever Has Fixed Establishment would navigate to" in {

          val hasFixedEstablishmentAnswer = arbitrary[Boolean].sample.value
          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), hasFixedEstablishmentAnswer).success.value

          EuVatNumberPage(index).navigate(CheckMode, answers)
            .mustEqual(HasFixedEstablishmentPage(index).navigate(CheckMode, answers))
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when Has Fixed Establishment has not been answered" - {

        "to Has Fixed Establishment" in {

          EuVatNumberPage(index).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(CheckLoopMode, index))
        }
      }

      "when Has Fixed Establishment has been answered" - {

        "to wherever Has Fixed Establishment would navigate to" in {

          val hasFixedEstablishmentAnswer = arbitrary[Boolean].sample.value
          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), hasFixedEstablishmentAnswer).success.value

          EuVatNumberPage(index).navigate(CheckLoopMode, answers)
            .mustEqual(HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers))
        }
      }
    }
  }
}
