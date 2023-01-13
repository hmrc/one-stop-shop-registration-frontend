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

package forms.mappings

import models.{Country, PreviousScheme, PreviousSchemeType}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.validation.{Invalid, Valid}

class PreviousRegistrationSchemeConstraintSpec extends AnyFreeSpec with Matchers with PreviousRegistrationSchemeConstraint {

  private val country = Country.euCountries.head

  "validatePreviousRegistrationScheme" - {

    "must return Valid when one OSS scheme is passed" in {

      val previousOssScheme: Seq[PreviousScheme] = Seq(
        PreviousScheme.OSSU
      )
      val result = validatePreviousRegistrationSchemes(country.name,previousOssScheme,
        "previousScheme.oss.exceed.error", "previousScheme.ioss.exceed.error")(PreviousSchemeType.OSS)
      result mustEqual Valid
    }

    "must return Invalid when two OSS schemes are passed" in {

      val previousOssScheme: Seq[PreviousScheme] = Seq(
        PreviousScheme.OSSU, PreviousScheme.OSSNU
      )
      val result = validatePreviousRegistrationSchemes(country.name, previousOssScheme,
        "previousScheme.oss.exceed.error", "previousScheme.ioss.exceed.error")(PreviousSchemeType.OSS)
      result mustEqual Invalid("previousScheme.oss.exceed.error", country.name)
    }

    "must return Invalid when IOSS scheme is passed" in {

      val previousIossScheme: Seq[PreviousScheme] = Seq(
        PreviousScheme.IOSSWI
      )
      val result = validatePreviousRegistrationSchemes(country.name, previousIossScheme,
        "previousScheme.oss.exceed.error", "previousScheme.ioss.exceed.error")(PreviousSchemeType.IOSS)
      result mustEqual Invalid("previousScheme.ioss.exceed.error", country.name)
    }
  }

}
