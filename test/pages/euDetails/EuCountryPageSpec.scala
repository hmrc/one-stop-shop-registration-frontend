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
import models.{AmendMode, CheckLoopMode, CheckMode, Country, Index, NormalMode, RejoinMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.behaviours.PageBehaviours
import pages.euDetails


class EuCountryPageSpec extends SpecBase with PageBehaviours with ScalaCheckPropertyChecks {

  private val countryIndex = Index(0)

  "EuCountryPage" - {

    beRetrievable[Country](EuCountryPage(countryIndex))

    beSettable[Country](euDetails.EuCountryPage(countryIndex))

    beRemovable[Country](euDetails.EuCountryPage(countryIndex))

    "must navigate in Normal mode" - {

      "to Sells Goods to EU Consumer for the same index" in {

        EuCountryPage(countryIndex).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumersController.onPageLoad(NormalMode, countryIndex))
      }
    }

    "must navigate in Check mode" - {

      "when Sells Goods to EU Consumers has not been answered" - {

        "to Sells Goods to EU Consumers for the same index" in {

          EuCountryPage(countryIndex).navigate(CheckMode, emptyUserAnswers)
            .mustEqual(euRoutes.SellsGoodsToEUConsumersController.onPageLoad(CheckMode, countryIndex))
        }
      }

      "when Sells Goods to EU Consumers has been answered" - {

        "to wherever Sells Goods to EU Consumers would navigate to in Check mode" in {

          forAll(arbitrary[UserAnswers]) {
            baseAnswers =>

              val answers = baseAnswers
                .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value

              EuCountryPage(countryIndex).navigate(CheckMode, answers)
                .mustEqual(SellsGoodsToEUConsumersPage(Index(0)).navigate(CheckMode, answers))
          }
        }
      }
    }

    "must navigate in Amend mode" - {

      "when Sells Goods to EU Consumers has not been answered" - {

        "to Sells Goods to EU Consumers for the same index" in {

          EuCountryPage(countryIndex).navigate(AmendMode, emptyUserAnswers)
            .mustEqual(euRoutes.SellsGoodsToEUConsumersController.onPageLoad(AmendMode, countryIndex))
        }
      }

      "when Sells Goods to EU Consumers has been answered" - {

        "to wherever Sells Goods to EU Consumers would navigate to in Amend mode" in {

          forAll(arbitrary[UserAnswers]) {
            baseAnswers =>

              val answers = baseAnswers
                .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value

              EuCountryPage(countryIndex).navigate(AmendMode, answers)
                .mustEqual(SellsGoodsToEUConsumersPage(Index(0)).navigate(AmendMode, answers))
          }
        }
      }
    }

    "must navigate in CheckLoop mode" - {

      "when Sells Goods to EU Consumers has not been answered" - {

        "to Sells Goods to EU Consumers for the same index" in {

          EuCountryPage(countryIndex).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(euRoutes.SellsGoodsToEUConsumersController.onPageLoad(CheckLoopMode, countryIndex))
        }
      }

      "when Sells Goods to EU Consumers has been answered" - {

        "to wherever Sells Goods to EU Consumers would navigate to in Check mode" in {

          forAll(arbitrary[UserAnswers]) {
            baseAnswers =>

              val answers = baseAnswers
                .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value

              EuCountryPage(countryIndex).navigate(CheckLoopMode, answers)
                .mustEqual(SellsGoodsToEUConsumersPage(Index(0)).navigate(CheckLoopMode, answers))
          }
        }
      }
    }

    "must navigate in Rejoin mode" - {

      "when Sells Goods to EU Consumers has not been answered" - {

        "to Sells Goods to EU Consumers for the same index" in {

          EuCountryPage(countryIndex).navigate(RejoinMode, emptyUserAnswers)
            .mustEqual(euRoutes.SellsGoodsToEUConsumersController.onPageLoad(RejoinMode, countryIndex))
        }
      }

      "when Sells Goods to EU Consumers has been answered" - {

        "to wherever Sells Goods to EU Consumers would navigate to in Check mode" in {

          forAll(arbitrary[UserAnswers]) {
            baseAnswers =>

              val answers = baseAnswers
                .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value

              EuCountryPage(countryIndex).navigate(RejoinMode, answers)
                .mustEqual(SellsGoodsToEUConsumersPage(Index(0)).navigate(RejoinMode, answers))
          }
        }
      }
    }
  }
}
