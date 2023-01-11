/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{Country, Index, NormalMode}
import controllers.previousRegistrations.{routes => prevRegRoutes}
import models.previousRegistrations.PreviousSchemeNumbers
import pages.behaviours.PageBehaviours

class DeletePreviousSchemePageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)
  private val index1 = Index(1)

  "DeletePreviousSchemePage" - {

    "must navigate in Normal mode" - {

      "when there is a single country with multiple previous schemes remaining" - {

        "redirect to Check Previous Scheme Answers Page" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
              .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("FR123", None)).success.value
              .set(PreviousOssNumberPage(index,index1), PreviousSchemeNumbers("FR234", None)).success.value

          println("ANSWERS1: " + answers)

          DeletePreviousSchemePage(index).navigate(NormalMode, answers)
            .mustEqual(prevRegRoutes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index))
        }
      }

      "when there are multiple countries with multiple previous schemes remaining" - {

        "redirect to Add Previous Registration Page" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
                .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("FR123", None)).success.value
                .set(PreviousOssNumberPage(index, index1), PreviousSchemeNumbers("FR234", None)).success.value
              .set(PreviousEuCountryPage(index1), Country("DE", "Germany")).success.value
                .set(PreviousOssNumberPage(index1, index), PreviousSchemeNumbers("DE123", None)).success.value

          println("ANSWERS2: " + answers)

          DeletePreviousSchemePage(index1).navigate(NormalMode, answers)
            .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode))
        }
      }

      "when there is no previous scheme remaining" - {

        "redirect to Previously Registered Page" in {

          val answers =
            emptyUserAnswers

          DeletePreviousSchemePage(index).navigate(NormalMode, answers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
        }
      }
    }

    //TODO
    "must navigate in Check mode" - {

    }
  }
}
