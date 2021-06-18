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

import models.CurrentlyRegisteredInCountry.{No, Yes}
import models.StartDateOption.EarlierDate
import models._
import models.domain.{EuTaxIdentifier, EuTaxIdentifierType, FixedEstablishment}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

trait ModelGenerators {

  implicit lazy val arbitraryIban: Arbitrary[Iban] =
    Arbitrary {
      Gen.oneOf(
        "GB94BARC10201530093459",
        "GB33BUKB20201555555555",
        "DE29100100100987654321",
        "GB24BKEN10000031510604",
        "GB27BOFI90212729823529",
        "GB17BOFS80055100813796",
        "GB92BARC20005275849855",
        "GB66CITI18500812098709",
        "GB15CLYD82663220400952",
        "GB26MIDL40051512345674",
        "GB76LOYD30949301273801",
        "GB25NWBK60080600724890",
        "GB60NAIA07011610909132",
        "GB29RBOS83040210126939",
        "GB79ABBY09012603367219",
        "GB21SCBL60910417068859",
        "GB42CPBK08005470328725"
      ).map(v => Iban(v).right.get)
    }

  implicit lazy val arbitraryCurrentlyRegisteredInCountry: Arbitrary[CurrentlyRegisteredInCountry] =
    Arbitrary {
      Gen.oneOf(Gen.const(No), arbitrary[Country].map(Yes))
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1         <- arbitrary[String]
        line2         <- Gen.option(arbitrary[String])
        townOrCity    <- arbitrary[String]
        stateOrRegion <- Gen.option(arbitrary[String])
        postCode      <- Gen.option(arbitrary[String])
        country       <- arbitrary[Country]
      } yield InternationalAddress(line1, line2, townOrCity, stateOrRegion, postCode, country)
    }

  implicit val arbitraryEuTaxIdentifierType: Arbitrary[EuTaxIdentifierType] =
    Arbitrary {
      Gen.oneOf(EuTaxIdentifierType.values)
    }

  implicit val arbitraryEuTaxIdentifier: Arbitrary[EuTaxIdentifier] =
    Arbitrary {
      for {
        identifierType <- arbitrary[EuTaxIdentifierType]
        value          <- arbitrary[Int].map(_.toString)
      } yield EuTaxIdentifier(identifierType, value)
    }

  implicit lazy val arbitraryCheckVatDetails: Arbitrary[CheckVatDetails] =
    Arbitrary {
      Gen.oneOf(CheckVatDetails.values)
    }

  implicit lazy val arbitraryBankDetails: Arbitrary[BankDetails] =
    Arbitrary {
      for {
        accountName <- arbitrary[String]
        bic <- Gen.option(Gen.listOfN(11, Gen.alphaNumChar).map(_.mkString))
        ibanChars <- Gen.choose(5, 34)
        iban <- arbitrary[Iban]
      } yield BankDetails(accountName, bic, iban)
    }

  implicit lazy val arbitraryFixedEstablishment: Arbitrary[FixedEstablishment] =
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
        address     <- arbitrary[InternationalAddress]
      } yield FixedEstablishment(tradingName, address)
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      Gen.oneOf(Country.euCountries)
    }

  implicit lazy val arbitraryStartDate: Arbitrary[StartDate] = {
    Arbitrary {
      Gen.const(StartDate(EarlierDate, LocalDate.now))
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

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1      <- arbitrary[String]
        line2      <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county     <- Gen.option(arbitrary[String])
        postCode   <- arbitrary[String]
      } yield UkAddress(line1, line2, townOrCity, county, postCode)
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
