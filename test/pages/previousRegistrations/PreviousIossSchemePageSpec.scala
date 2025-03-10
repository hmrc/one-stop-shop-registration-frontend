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

package pages.previousRegistrations

import base.SpecBase
import models.{AmendMode, CheckMode, Index, NormalMode, RejoinMode}
import pages.behaviours.PageBehaviours

class PreviousIossSchemePageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "PreviousIossSchemePage" - {

    beRetrievable[Boolean](PreviousIossSchemePage(index, index))

    beSettable[Boolean](PreviousIossSchemePage(index, index))

    beRemovable[Boolean](PreviousIossSchemePage(index, index))

    "must navigate in Normal mode" - {

      "to Previous IOSS scheme number" in {

        PreviousIossSchemePage(index, index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(controllers.previousRegistrations.routes.PreviousIossNumberController.onPageLoad(NormalMode, index, index))
      }

    }

    "must navigate in Check mode" - {

      "to Previous IOSS scheme number" in {

        PreviousIossSchemePage(index, index).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(controllers.previousRegistrations.routes.PreviousIossNumberController.onPageLoad(CheckMode, index, index))
      }

    }

    "must navigate in Amend mode" - {

      "to Previous IOSS scheme number" in {

        PreviousIossSchemePage(index, index).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(controllers.previousRegistrations.routes.PreviousIossNumberController.onPageLoad(AmendMode, index, index))
      }

    }

    "must navigate in Rejoin mode" - {

      "to Previous IOSS scheme number" in {

        PreviousIossSchemePage(index, index).navigate(RejoinMode, emptyUserAnswers)
          .mustEqual(controllers.previousRegistrations.routes.PreviousIossNumberController.onPageLoad(RejoinMode, index, index))
      }

    }

  }
}
