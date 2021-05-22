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

package generators

import models.StartDateOption.{EarlierDate, NextPeriod}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

trait ModelGenerators {

  implicit lazy val arbitraryStartDate: Arbitrary[StartDate] = {
    val nextPeriodGen = Gen.const(StartDateOption.NextPeriod)

    val earlierDateGen = Gen.const(LocalDate.now())

    Arbitrary {
      Gen.oneOf(
        nextPeriodGen.map(_ => StartDate(NextPeriod, None)),
        earlierDateGen.map(date => StartDate(EarlierDate, Some(date)))
      )
    }
  }

  implicit lazy val arbitraryBusinessContactDetails: Arbitrary[BusinessContactDetails] =
    Arbitrary {
      for {
        fullName <- arbitrary[String]
        telephoneNumber <- arbitrary[String]
        emailAddress <- arbitrary[String]
      } yield BusinessContactDetails(fullName, telephoneNumber, emailAddress)
    }

  implicit lazy val arbitraryBusinessAddress: Arbitrary[BusinessAddress] =
    Arbitrary {
      for {
        line1      <- arbitrary[String]
        line2      <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county     <- Gen.option(arbitrary[String])
        postCode   <- arbitrary[String]
      } yield BusinessAddress(line1, line2, townOrCity, county, postCode)
    }

  implicit def arbitraryVrn: Arbitrary[Vrn] = Arbitrary {
    for {
      prefix <- Gen.oneOf("", "GB")
      chars  <- Gen.listOfN(9, Gen.numChar)
    } yield {
      Vrn(prefix + chars.mkString(""))
    }
  }
}
