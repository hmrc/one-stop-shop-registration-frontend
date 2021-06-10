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

package queries

import base.SpecBase
import models.{Country, Index, UserAnswers}
import pages.euDetails.{EuCountryPage, EuVatNumberPage, HasFixedEstablishmentPage, VatRegisteredPage}
import pages.{CurrentCountryOfRegistrationPage, CurrentlyRegisteredInCountryPage, CurrentlyRegisteredInEuPage}

class EuDetailsQuerySpec extends SpecBase {

  private def country(index: Int) = Country.euCountries(index)

  implicit class RichUserAnswers(answers: UserAnswers) {
    def addVatRegisteredCountry(index: Index): UserAnswers =
      answers
        .set(EuCountryPage(index), country(index.position)).success.value
        .set(VatRegisteredPage(index), true).success.value
        .set(EuVatNumberPage(index), "123").success.value
        .set(HasFixedEstablishmentPage(index), false).success.value

    def addNonVatRegisteredCountry(index: Index): UserAnswers =
      answers
        .set(EuCountryPage(index), country(index.position)).success.value
        .set(VatRegisteredPage(index), false).success.value
        .set(HasFixedEstablishmentPage(index), false).success.value
  }


  "EuDetailsQuery" - {

    "when removing a country" - {

      "and no countries remain" - {

        "must remove Currently Registered in Country" in {

          val answers =
            emptyUserAnswers
              .addVatRegisteredCountry(Index(0))
              .set(CurrentlyRegisteredInCountryPage, true).success.value

          val result = answers.remove(EuDetailsQuery(Index(0))).success.value

          result.get(EuDetailsQuery(Index(0))) must not be defined
          result.get(CurrentlyRegisteredInCountryPage) must not be defined
        }
      }

      "and no VAT registered countries remain" - {

        "must remove all current registration details" in {

          val answers =
            emptyUserAnswers
              .addNonVatRegisteredCountry(Index(0))
              .addVatRegisteredCountry(Index(1))
              .set(CurrentlyRegisteredInCountryPage, true).success.value
              .set(CurrentlyRegisteredInEuPage, true).success.value
              .set(CurrentCountryOfRegistrationPage, country(1)).success.value

          val result = answers.remove(EuDetailsQuery(Index(1))).success.value

          result.get(EuDetailsQuery(Index(0))) mustBe defined
          result.get(EuDetailsQuery(Index(1))) must not be defined
          result.get(CurrentlyRegisteredInCountryPage) must not be defined
          result.get(CurrentlyRegisteredInEuPage) must not be defined
          result.get(CurrentCountryOfRegistrationPage) must not be defined
        }
      }

      "and one VAT registered country remains" - {

        "must not remove Currently Registered in Country" in {

          val answers =
            emptyUserAnswers
              .addNonVatRegisteredCountry(Index(0))
              .addVatRegisteredCountry(Index(1))
              .set(CurrentlyRegisteredInCountryPage, true).success.value

          val result = answers.remove(EuDetailsQuery(Index(0))).success.value

          result.get(CurrentlyRegisteredInCountryPage) mustBe defined
        }

        "and the remaining country is the Current Country of Registration" - {

          "must not remove Currently Registered in EU or Current Country of Registration" in {

            val answers =
              emptyUserAnswers
                .addNonVatRegisteredCountry(Index(0))
                .addVatRegisteredCountry(Index(1))
                .set(CurrentlyRegisteredInEuPage, true).success.value
                .set(CurrentCountryOfRegistrationPage, country(1)).success.value

            val result = answers.remove(EuDetailsQuery(Index(0))).success.value

            result.get(CurrentlyRegisteredInEuPage).value mustEqual true
            result.get(CurrentCountryOfRegistrationPage).value mustEqual country(1)
          }
        }

        "and the remaining country is not the Current Country of Registration" - {

          "must remove Currently Registered in EU or Current Country of Registration" in {

            val answers =
              emptyUserAnswers
                .addNonVatRegisteredCountry(Index(0))
                .addVatRegisteredCountry(Index(1))
                .set(CurrentlyRegisteredInEuPage, true).success.value
                .set(CurrentCountryOfRegistrationPage, country(2)).success.value

            val result = answers.remove(EuDetailsQuery(Index(0))).success.value

            result.get(CurrentlyRegisteredInEuPage) must not be defined
            result.get(CurrentCountryOfRegistrationPage) must not be defined
          }
        }
      }

      "and multiple VAT registered countries remain" - {

        "and the Current Country of Registration is in the remaining countries" - {

          "must not remove Currently Registered in EU or Current Country of Registration" in {

            val answers =
              emptyUserAnswers
                .addVatRegisteredCountry(Index(0))
                .addVatRegisteredCountry(Index(1))
                .addVatRegisteredCountry(Index(2))
                .set(CurrentlyRegisteredInEuPage, true).success.value
                .set(CurrentCountryOfRegistrationPage, country(1)).success.value

            val result = answers.remove(EuDetailsQuery(Index(0))).success.value

            result.get(CurrentlyRegisteredInEuPage).value mustEqual true
            result.get(CurrentCountryOfRegistrationPage).value mustEqual country(1)
          }
        }

        "and the Current Country of Registration is not in the remaining countries" - {

          "must remove Currently Registered in EU or Current Country of Registration" in {

            val answers =
              emptyUserAnswers
                .addVatRegisteredCountry(Index(0))
                .addVatRegisteredCountry(Index(1))
                .addVatRegisteredCountry(Index(2))
                .set(CurrentlyRegisteredInEuPage, true).success.value
                .set(CurrentCountryOfRegistrationPage, country(0)).success.value

            val result = answers.remove(EuDetailsQuery(Index(0))).success.value

            result.get(CurrentlyRegisteredInEuPage) must not be defined
            result.get(CurrentCountryOfRegistrationPage) must not be defined
          }
        }
      }
    }
  }
}
