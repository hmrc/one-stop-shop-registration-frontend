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

package forms

import forms.behaviours.OptionFieldBehaviours
import models.euVatDetails.Country
import play.api.data.FormError

class CurrentCountryOfRegistrationFormProviderSpec extends OptionFieldBehaviours {

  val countries = Country.euCountries.take(10)
  val form = new CurrentCountryOfRegistrationFormProvider()(countries)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "currentCountryOfRegistration.error.required"


    "bind all countries provided to the form" in {

      for(value <- countries) {

        val result = form.bind(Map(fieldName -> value.code)).apply(fieldName)
        result.value.value mustEqual value.code
        result.errors mustBe empty
      }
    }

    "not bind invalid values" in {

      val generator = stringsExceptSpecificValues(countries.map(_.code))

      forAll(generator -> "invalidValue") {
        value =>

          val result = form.bind(Map(fieldName -> value)).apply(fieldName)
          result.errors must contain only FormError(fieldName, "currentCountryOfRegistration.error.required")
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
