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

import models.{FixedEstablishmentAddress, Index, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class HasFixedEstablishmentPageSpec extends PageBehaviours {

  private val index = Index(0)

  "HasFixedEstablishmentPage" - {

    beRetrievable[Boolean](HasFixedEstablishmentPage(index))

    beSettable[Boolean](HasFixedEstablishmentPage(index))

    beRemovable[Boolean](HasFixedEstablishmentPage(index))

    "must remove Fixed Establishment Trading Name and Address for this index when the answer is no" in {

      val address1 = arbitrary[FixedEstablishmentAddress].sample.value
      val address2 = arbitrary[FixedEstablishmentAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address1).success.value
          .set(FixedEstablishmentTradingNamePage(Index(1)), "second").success.value
          .set(FixedEstablishmentAddressPage(Index(1)), address2).success.value

      val result = answers.set(HasFixedEstablishmentPage(Index(1)), false).success.value

      result.get(FixedEstablishmentTradingNamePage(Index(0))).value mustEqual "first"
      result.get(FixedEstablishmentAddressPage(Index(0))).value mustEqual address1
      result.get(FixedEstablishmentTradingNamePage(Index(1))) mustBe empty
      result.get(FixedEstablishmentAddressPage(Index(1))) mustBe empty
    }

    "must preserve Fixed Establishment Trading Name and Address when the answer is no" in {

      val address = arbitrary[FixedEstablishmentAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address).success.value

      val result = answers.set(HasFixedEstablishmentPage(Index(0)), true).success.value

      result.get(FixedEstablishmentTradingNamePage(Index(0))).value mustEqual "first"
      result.get(FixedEstablishmentAddressPage(Index(0))).value mustEqual address
    }
  }
}
