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
import controllers.previousRegistrations.routes as prevRegRoutes
import models.domain.PreviousSchemeNumbers
import models.{AmendMode, CheckMode, Country, Index, NormalMode, RejoinMode}
import pages.behaviours.PageBehaviours
import queries.previousRegistration.PreviousSchemeForCountryQuery

import scala.concurrent.Future

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
              .set(PreviousOssNumberPage(index, index1), PreviousSchemeNumbers("FR234", None)).success.value

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index, index1)))

          DeletePreviousSchemePage(index).navigate(NormalMode, updatedAnswers.futureValue)
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

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index1, index)))

          DeletePreviousSchemePage(index1).navigate(NormalMode, updatedAnswers.futureValue)
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

    "must navigate in Check mode" - {

      "when there is a single country with multiple previous schemes remaining" - {

        "redirect to Check Previous Scheme Answers Page" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
              .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("FR123", None)).success.value
              .set(PreviousOssNumberPage(index, index1), PreviousSchemeNumbers("FR234", None)).success.value

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index, index1)))

          DeletePreviousSchemePage(index).navigate(CheckMode, updatedAnswers.futureValue)
            .mustEqual(prevRegRoutes.CheckPreviousSchemeAnswersController.onPageLoad(CheckMode, index))
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

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index1, index)))

          DeletePreviousSchemePage(index1).navigate(CheckMode, updatedAnswers.futureValue)
            .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(CheckMode))
        }
      }

      "when there is no previous scheme remaining" - {

        "redirect to Previously Registered Page" in {

          val answers =
            emptyUserAnswers

          DeletePreviousSchemePage(index).navigate(CheckMode, answers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(CheckMode))
        }
      }

    }

    "must navigate in Amend mode" - {

      "when there is a single country with multiple previous schemes remaining" - {

        "redirect to Check Previous Scheme Answers Page" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
              .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("FR123", None)).success.value
              .set(PreviousOssNumberPage(index, index1), PreviousSchemeNumbers("FR234", None)).success.value

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index, index1)))

          DeletePreviousSchemePage(index).navigate(AmendMode, updatedAnswers.futureValue)
            .mustEqual(prevRegRoutes.CheckPreviousSchemeAnswersController.onPageLoad(AmendMode, index))
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

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index1, index)))

          DeletePreviousSchemePage(index1).navigate(AmendMode, updatedAnswers.futureValue)
            .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(AmendMode))
        }
      }

      "when there is no previous scheme remaining" - {

        "redirect to Previously Registered Page" in {

          val answers =
            emptyUserAnswers

          DeletePreviousSchemePage(index).navigate(AmendMode, answers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(AmendMode))
        }
      }

    }

    "must navigate in Rejoin mode" - {

      "when there is a single country with multiple previous schemes remaining" - {

        "redirect to Check Previous Scheme Answers Page" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
              .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("FR123", None)).success.value
              .set(PreviousOssNumberPage(index, index1), PreviousSchemeNumbers("FR234", None)).success.value

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index, index1)))

          DeletePreviousSchemePage(index).navigate(RejoinMode, updatedAnswers.futureValue)
            .mustEqual(prevRegRoutes.CheckPreviousSchemeAnswersController.onPageLoad(RejoinMode, index))
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

          val updatedAnswers = Future.fromTry(answers.remove(PreviousSchemeForCountryQuery(index1, index)))

          DeletePreviousSchemePage(index1).navigate(RejoinMode, updatedAnswers.futureValue)
            .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(RejoinMode))
        }
      }

      "when there is no previous scheme remaining" - {

        "redirect to Previously Registered Page" in {

          val answers =
            emptyUserAnswers

          DeletePreviousSchemePage(index).navigate(RejoinMode, answers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(RejoinMode))
        }
      }

    }

  }
}
