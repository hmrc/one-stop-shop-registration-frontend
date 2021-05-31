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

import models.UserAnswers
import models.euVatDetails.Country
import pages.behaviours.PageBehaviours

class CurrentlyRegisteredInEuPageSpec extends PageBehaviours {

  "CurrentlyRegisteredInEuPage" - {

    beRetrievable[Boolean](CurrentlyRegisteredInEuPage)

    beSettable[Boolean](CurrentlyRegisteredInEuPage)

    beRemovable[Boolean](CurrentlyRegisteredInEuPage)

    "must remove Current Country of Registration if the answer is no" in {

      val answers = UserAnswers("id").set(CurrentCountryOfRegistrationPage, Country.euCountries.head).success.value
      val result = answers.set(CurrentlyRegisteredInEuPage, false).success.value

      result.get(CurrentCountryOfRegistrationPage) must not be defined
    }
  }
}
