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

package services

import base.SpecBase
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import testutils.RegistrationData

class RegistrationServiceSpec
  extends SpecBase
    with MockitoSugar
    with ScalaCheckPropertyChecks
    with OptionValues {

  ".toUserAnswers" - {

    "normal registration returns good user answers for all pages" - {

      val service = new RegistrationService()

      val result = service.toUserAnswers(userAnswersId, RegistrationData.registration, vatCustomerInfo).futureValue

      result mustBe completeUserAnswers
    }

  }

}
