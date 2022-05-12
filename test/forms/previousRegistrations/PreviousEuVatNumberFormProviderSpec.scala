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

package forms.previousRegistrations

import forms.Validation.Validation.euVatNumberPattern
import forms.behaviours.StringFieldBehaviours
import models.Country
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.FormError

class PreviousEuVatNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "previousEuVatNumber.error.required"
  val invalidKey = "previousEuVatNumber.error.invalid"
  val validData = "DE+854123"
  val validLowerCaseData = "de+854123"
  val maxLength = 12

  val country: Country = arbitrary[Country].sample.value

  val formProvider: PreviousEuVatNumberFormProvider = new PreviousEuVatNumberFormProvider()
  val form = formProvider(country)
  val countriesAndValidVatNumbers = Seq(
    (Country("AT", "Austria"), Seq("U23456789") ),
    (Country("BE", "Belgium"), Seq("0123456789", "1123456789")),
    (Country("BG", "Bulgaria"), Seq("123456789", "1234567890")),
    (Country("HR", "Croatia"), Seq("12345678901")),
    (Country("CY", "Republic of Cyprus"), Seq("12345678L")),
    (Country("CZ", "Czech Republic"), Seq("12345678", "123456789", "1234567890")),
    (Country("DK", "Denmark"), Seq("12345678")),
    (Country("EE", "Estonia"), Seq("123456789")),
    (Country("FI", "Finland"), Seq("12345678")),
    (Country("FR", "France"), Seq("AA123456789", "11123456789", "A1123456789", "1A123456789")),
    (Country("DE", "Germany"), Seq("123456789")),
    (Country("EL", "Greece"), Seq("123456789")),
    (Country("HU", "Hungary"), Seq("12345678")),
    (Country("IE", "Ireland"), Seq("1A12345L", "1112345L", "1+12345L", "1*12345L", "1234567WI" )),
    (Country("IT", "Italy"), Seq("12345678901")),
    (Country("LV", "Latvia"), Seq("12345678901")),
    (Country("LT", "Lithuania"), Seq("123456789", "123456789012")),
    (Country("LU", "Luxembourg"), Seq("12345678")),
    (Country("MT", "Malta"), Seq("12345678")),
    (Country("NL", "Netherlands"), Seq("123456789012", "A+*456789012", "++++++++++++", "************", "AAAAAAAAAAAA")),
    (Country("PL", "Poland"), Seq("1234567890")),
    (Country("PT", "Portugal"), Seq("123456789")),
    (Country("RO", "Romania"), Seq("12", "123", "1234", "12345", "123456", "1234567", "12345678", "123456789", "1234567890")),
    (Country("SK", "Slovakia"), Seq("1234567890")),
    (Country("SI", "Slovenia"), Seq("12345678")),
    (Country("ES", "Spain"), Seq("A12345678", "12345678A", "A1234567A")),
    (Country("SE", "Sweden"), Seq("123456789012"))
  )

  val countriesAndInvalidVatNumbers = Seq(
    (Country("AT", "Austria"), Seq("123456789", "U23456A89", "U234567890", "U2345678") ),
    (Country("BE", "Belgium"), Seq("2123456789", "112345678", "11234567890", "112345678A")),
    (Country("BG", "Bulgaria"), Seq("12345678", "12345678900", "123AAA789A")),
    (Country("HR", "Croatia"), Seq("1234567890", "123456789010", "123456789AA")),
    (Country("CY", "Republic of Cyprus"), Seq("123456781", "1234567L", "1234567LL", "123456789L")),
    (Country("CZ", "Czech Republic"), Seq("1234567", "123AAA789", "12345678900")),
    (Country("DK", "Denmark"), Seq("1234567", "123456789", "123456AA")),
    (Country("EE", "Estonia"), Seq("12345678", "1234567890", "1234567AA")),
    (Country("FI", "Finland"), Seq("1234567", "123456789", "123456AA")),
    (Country("FR", "France"), Seq("AA1234567890", "*1123456789", "A11234567AA", "1A12345")),
    (Country("DE", "Germany"), Seq("12345678", "1234567890", "1234567AA")),
    (Country("EL", "Greece"), Seq("12345678", "1234567890", "12345678A")),
    (Country("HU", "Hungary"), Seq("1234567", "123456789", "123456AA")),
    (Country("IE", "Ireland"), Seq("1A123451", "A112345L", "1+1234445L", "1*12A45L", "121134567WI", "12234567890" )),
    (Country("IT", "Italy"), Seq("1234567890", "123456789010", "123456789AA")),
    (Country("LV", "Latvia"), Seq("1234567890", "123456789010", "123456789AA")),
    (Country("LT", "Lithuania"), Seq("12345678", "1234567890120", "12345678A")),
    (Country("LU", "Luxembourg"), Seq("1234567", "123456780", "123456AA")),
    (Country("MT", "Malta"), Seq("1234567", "123456780", "123456AA")),
    (Country("NL", "Netherlands"), Seq("12345678", "AAAAAAAAAAAAA")),
    (Country("PL", "Poland"), Seq("123456789", "12345678900", "12345678AA")),
    (Country("PT", "Portugal"), Seq("12345678", "1234567890", "1234567AA")),
    (Country("RO", "Romania"), Seq("1", "12AAA*6","12345678900")),
    (Country("SK", "Slovakia"), Seq("123456789", "12345678900", "12345678AA")),
    (Country("SI", "Slovenia"), Seq("1234567", "123456780", "123456AA")),
    (Country("ES", "Spain"), Seq("112345678", "123456781A", "1234567A")),
    (Country("SE", "Sweden"), Seq("12345678901", "1234567890120", "1234567AA012"))
  )

  ".value" - {

    val fieldName = "value"

    countriesAndValidVatNumbers.foreach{
      case (country, vatNumbers) =>
        vatNumbers.foreach { vatNumber =>
          s"must bind valid vat number ${vatNumber} for ${country.name}" - {
            val form = formProvider(country)
            behave like fieldThatBindsValidData(
              form,
              fieldName,
              vatNumber
            )
          }
        }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(country.name))
    )

    countriesAndInvalidVatNumbers.foreach{
      case (country, vatNumbers) =>
        vatNumbers.foreach {
          vatNumber =>
            s"must not bind invalid EU VAT number ${vatNumber} for ${country.name}" in {
              val form = formProvider(country)
              val result = form.bind(Map(fieldName -> vatNumber)).apply(fieldName)
              result.errors mustBe Seq(FormError(fieldName, invalidKey))
            }
        }
    }

  }
}
