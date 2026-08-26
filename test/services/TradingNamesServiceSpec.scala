/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import base.SpecBase
import models.{CheckVatDetails, CompositeAccount}
import models.etmp.intermediary.{EtmpIntermediaryDisplayRegistration, EtmpTradingName, IntermediaryRegistrationWrapper}
import models.iossRegistration.IossEtmpDisplayRegistration
import org.scalacheck.Arbitrary
import pages.HasTradingNamePage
import queries.AllTradingNames
import testutils.GenerateCompositeAccount.generateCompositeAccount

class TradingNamesServiceSpec extends SpecBase {

  private val service = new TradingNamesService()
  private val registrationWrapper: IntermediaryRegistrationWrapper = {
    val etmpDisplayRegistration =
      Arbitrary.arbitrary[EtmpIntermediaryDisplayRegistration].sample.value.copy(
        tradingNames = Seq(
          EtmpTradingName("Trading Name 1"),
          EtmpTradingName("Trading Name 2")
        )
      )

    IntermediaryRegistrationWrapper(
      intermediaryVatCustomerInfo,
      etmpDisplayRegistration
    )
  }

  "TradingNamesService" - {

    "must populate trading name answers from the IOSS registration when CheckVatDetails is Yes" in {

      val registration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

      val compositeAccount: Option[CompositeAccount] = generateCompositeAccount(Some(registration))

      val result = service.updateTradingNameAnswers(
        CheckVatDetails.Yes,
        emptyUserAnswers,
        compositeAccount
      ).success.value

      result.get(HasTradingNamePage).value mustBe true
      result.get(AllTradingNames).value mustBe
        registration.tradingNames.map(_.tradingName).toList
    }

    "must populate trading name answers from the intermediary registration when no IOSS trading names are available" in {

      val compositeAccount: Option[CompositeAccount] = generateCompositeAccount(intermediaryRegistration = Some(registrationWrapper))
      
      val result = service.updateTradingNameAnswers(
        CheckVatDetails.Yes,
        emptyUserAnswers,
        compositeAccount
      ).success.value

      result.get(HasTradingNamePage).value mustBe true
      result.get(AllTradingNames).value mustBe
        registrationWrapper.etmpDisplayRegistration.tradingNames
          .map(_.tradingName)
          .toList
    }

    "must prefer IOSS trading names when both registrations contain trading names" in {

      val iossRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

      val compositeAccount: Option[CompositeAccount] = generateCompositeAccount(
        iossRegistration = Some(iossRegistration),
        intermediaryRegistration = Some(registrationWrapper)
      )

      val result = service.updateTradingNameAnswers(
        CheckVatDetails.Yes,
        emptyUserAnswers,
        compositeAccount
      ).success.value

      result.get(AllTradingNames).value mustBe
        iossRegistration.tradingNames.map(_.tradingName).toList
    }

    "must leave user answers unchanged when CheckVatDetails is WrongAccount" in {

      val result = service.updateTradingNameAnswers(
        CheckVatDetails.WrongAccount,
        emptyUserAnswers,
        None
      ).success.value

      result mustBe emptyUserAnswers
    }

    "must leave user answers unchanged when CheckVatDetails is DetailsIncorrect" in {

      val result = service.updateTradingNameAnswers(
        CheckVatDetails.DetailsIncorrect,
        emptyUserAnswers,
        None
      ).success.value

      result mustBe emptyUserAnswers
    }

    "must leave user answers unchanged when no trading names are available" in {

      val iossRegistration =
        arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value
          .copy(tradingNames = Seq.empty)

      val compositeAccount: Option[CompositeAccount] = generateCompositeAccount(Some(iossRegistration))

      val result = service.updateTradingNameAnswers(
        CheckVatDetails.Yes,
        emptyUserAnswers,
        compositeAccount
      ).success.value

      result mustBe emptyUserAnswers
    }
  }
}