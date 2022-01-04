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

package generators

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.euDetails.{AddEuDetailsPage, EuCountryPage, EuTaxReferencePage, EuVatNumberPage, FixedEstablishmentAddressPage, FixedEstablishmentTradingNamePage, HasFixedEstablishmentPage, TaxRegisteredInEuPage, VatRegisteredPage}
import pages.previousRegistrations.{AddPreviousRegistrationPage, PreviousEuCountryPage, PreviousEuVatNumberPage, PreviouslyRegisteredPage}
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(IsPlanningFirstEligibleSalePage.type, JsValue)] ::
    arbitrary[(SalesChannelsPage.type, JsValue)] ::
    arbitrary[(HasFixedEstablishmentInNiPage.type, JsValue)] ::
    arbitrary[(BusinessBasedInNiPage.type, JsValue)] ::
    arbitrary[(IsOnlineMarketplacePage.type, JsValue)] ::
    arbitrary[(DateOfFirstSalePage.type, JsValue)] ::
    arbitrary[(InternationalAddressPage.type, JsValue)] ::
    arbitrary[(BusinessAddressInUkPage.type, JsValue)] ::
    arbitrary[(TaxRegisteredInEuPage.type, JsValue)] ::
    arbitrary[(HasWebsitePage.type, JsValue)] ::
    arbitrary[(EuTaxReferencePage, JsValue)] ::
    arbitrary[(BankDetailsPage.type, JsValue)] ::
    arbitrary[(PreviouslyRegisteredPage.type, JsValue)] ::
    arbitrary[(PreviousEuVatNumberPage, JsValue)] ::
    arbitrary[(PreviousEuCountryPage, JsValue)] ::
    arbitrary[(AddPreviousRegistrationPage.type, JsValue)] ::
    arbitrary[(HasFixedEstablishmentPage, JsValue)] ::
    arbitrary[(FixedEstablishmentTradingNamePage, JsValue)] ::
    arbitrary[(FixedEstablishmentAddressPage, JsValue)] ::
    arbitrary[(CheckVatDetailsPage.type, JsValue)] ::
    arbitrary[(CheckVatNumberPage.type, JsValue)] ::
    arbitrary[(WebsitePage, JsValue)] ::
    arbitrary[(BusinessContactDetailsPage.type, JsValue)] ::
    arbitrary[(UkAddressPage.type, JsValue)] ::
    arbitrary[(AddEuDetailsPage.type, JsValue)] ::
    arbitrary[(VatRegisteredPage, JsValue)] ::
    arbitrary[(EuCountryPage, JsValue)] ::
    arbitrary[(EuVatNumberPage, JsValue)] ::
    arbitrary[(UkVatEffectiveDatePage.type, JsValue)] ::
    arbitrary[(TradingNamePage, JsValue)] ::
    arbitrary[(RegisteredCompanyNamePage.type, JsValue)] ::
    arbitrary[(PartOfVatGroupPage.type, JsValue)] ::
    Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id      <- nonEmptyString
        data    <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers (
        id = id,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}
