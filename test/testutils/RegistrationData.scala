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

package testutils

import models.{BusinessAddress, BusinessContactDetails, Country, EuVatDetails, Registration}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

object RegistrationData {
  def createNewRegistration(): Registration =
    Registration(
      "foo",
      true,
      List("single", "double"),
      true,
      Vrn("GB123456789"),
      LocalDate.now(),
      "AA1 1AA",
      true,
      Seq(EuVatDetails(Country("FR", "France"),"FR123456789"), EuVatDetails(Country("ES", "Spain"),"ES123456789")),
      createBusinessAddress(),
      createBusinessContactDetails(),
      Seq("website1", "website2")
    )

  private def createBusinessAddress(): BusinessAddress =
    BusinessAddress(
      "123 Street",
      Some("Street"),
      "City",
      Some("county"),
      "AA12 1AB"
    )

  private def createBusinessContactDetails(): BusinessContactDetails =
    BusinessContactDetails(
      "Joe Bloggs",
      "01112223344",
      "email@email.com"
    )
}

