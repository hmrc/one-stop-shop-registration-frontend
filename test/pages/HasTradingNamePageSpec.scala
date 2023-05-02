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

package pages

import base.SpecBase
import controllers.amend.{routes => amendRoutes}
import controllers.routes
import models.{AmendMode, CheckMode, Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.behaviours.PageBehaviours

class HasTradingNamePageSpec extends SpecBase with PageBehaviours with MockitoSugar with BeforeAndAfterEach {

  "HasTradingNamePage" - {

    beRetrievable[Boolean](HasTradingNamePage)

    beSettable[Boolean](HasTradingNamePage)

    beRemovable[Boolean](HasTradingNamePage)

    "must navigate in Normal mode" - {

      "to Trading Name (index 0) when the answer is yes" in {

        val answers = emptyUserAnswers.set(HasTradingNamePage, true).success.value

        HasTradingNamePage.navigate(NormalMode, answers)
          .mustBe(routes.TradingNameController.onPageLoad(NormalMode, Index(0)))
      }

      "when the answer is no" - {

        "to Date of First Sale when the scheme has started" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, false).success.value

          HasTradingNamePage.navigate(NormalMode, answers)
            .mustBe(routes.HasMadeSalesController.onPageLoad(NormalMode))
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          HasTradingNamePage.navigate(NormalMode, emptyUserAnswers)
            .mustBe(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "to Trading name (index 0) when there are no trading names in the user's answers" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, true).success.value

          HasTradingNamePage.navigate(CheckMode, answers)
            .mustEqual(routes.TradingNameController.onPageLoad(CheckMode, Index(0)))
        }

        "to Add Trading Name when there are trading names in the user's answers" in {

          val answers =
            emptyUserAnswers
              .set(TradingNamePage(Index(0)), "foo trading name").success.value
              .set(HasTradingNamePage, true).success.value

          HasTradingNamePage.navigate(CheckMode, answers)
            .mustEqual(routes.AddTradingNameController.onPageLoad(CheckMode))
        }

      }

      "when the answer is no" - {

        "to Delete All Trading Names when there are trading names in the user's answers" in {

          val answers = emptyUserAnswers
            .set(HasTradingNamePage, false).success.value
            .set(TradingNamePage(Index(0)), "foo trading name").success.value
            .set(TradingNamePage(Index(1)), "bar trading name").success.value

          HasTradingNamePage.navigate(CheckMode, answers)
            .mustEqual(routes.DeleteAllTradingNamesController.onPageLoad(CheckMode))
        }

        "to Check Your Answers when there are no trading names in the user's answers" in {

          val answers = emptyUserAnswers
            .set(HasTradingNamePage, false).success.value

          HasTradingNamePage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }

      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          HasTradingNamePage.navigate(CheckMode, emptyUserAnswers)
            .mustBe(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Amend mode" - {

      "when the answer is yes" - {

        "to Trading name (index 0) when there are no trading names in the user's answers" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, true).success.value

          HasTradingNamePage.navigate(AmendMode, answers)
            .mustEqual(routes.TradingNameController.onPageLoad(AmendMode, Index(0)))
        }

        "to Add Trading name when there are trading names in the user's answers" in {

          val answers = emptyUserAnswers
            .set(HasTradingNamePage, true).success.value
            .set(TradingNamePage(Index(0)), "foo trading name").success.value
            .set(TradingNamePage(Index(1)), "bar trading name").success.value

          HasTradingNamePage.navigate(AmendMode, answers)
            .mustEqual(routes.AddTradingNameController.onPageLoad(AmendMode))
        }

      }

      "when the answer is no" - {

        "to Delete All Trading Names Page when there are trading names in the user's answers" in {

          val answers = emptyUserAnswers
            .set(HasTradingNamePage, false).success.value
            .set(TradingNamePage(Index(0)), "foo trading name").success.value
            .set(TradingNamePage(Index(1)), "bar trading name").success.value

          HasTradingNamePage.navigate(AmendMode, answers)
            .mustEqual(routes.DeleteAllTradingNamesController.onPageLoad(AmendMode))
        }

        "to Change Your Registration Page when there are no trading names in the user's answers" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, false).success.value

          HasTradingNamePage.navigate(AmendMode, answers)
            .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          HasTradingNamePage.navigate(AmendMode, emptyUserAnswers)
            .mustBe(routes.JourneyRecoveryController.onPageLoad())
        }
      }

    }
  }
}
