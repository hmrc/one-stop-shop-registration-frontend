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

import connectors.SavedUserAnswers
import models._
import models.domain.ModelHelpers.normaliseSpaces
import models.domain.{EuTaxIdentifier, EuTaxIdentifierType, TradeDetails}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{choose, listOfN}
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn

import java.time.Instant

trait ModelGenerators {

  implicit lazy val arbitraryPreviousSchemePage: Arbitrary[PreviousScheme] =
    Arbitrary {
      Gen.oneOf(PreviousScheme.values.toSeq)
    }

  implicit lazy val arbitrarySalesChannels: Arbitrary[SalesChannels] =
    Arbitrary {
      Gen.oneOf(SalesChannels.values)
    }

  implicit lazy val arbitraryBic: Arbitrary[Bic] = {
    val asciiCodeForA = 65
    val asciiCodeForN = 78
    val asciiCodeForP = 80
    val asciiCodeForZ = 90

    Arbitrary {
      for {
        firstChars <- Gen.listOfN(6, Gen.alphaUpperChar).map(_.mkString)
        char7      <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9)
        )
        lastChars <- Gen.option(Gen.listOfN(3, Gen.oneOf(Gen.alphaUpperChar, Gen.numChar)).map(_.mkString))
      } yield Bic(s"$firstChars$char7$char8${lastChars.getOrElse("")}").get
    }
  }
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

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1         <- commonFieldString(35)
        line2         <- Gen.option(commonFieldString(35))
        townOrCity    <- commonFieldString(35)
        stateOrRegion <- Gen.option(commonFieldString(35))
        postCode      <- Gen.option(arbitrary[String])
        country       <- Gen.oneOf(Country.internationalCountries)
      } yield InternationalAddress(normaliseSpaces(line1), normaliseSpaces(line2), normaliseSpaces(townOrCity), normaliseSpaces(stateOrRegion), normaliseSpaces(postCode), country)
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
        bic         <- Gen.option(arbitrary[Bic])
        iban        <- arbitrary[Iban]
      } yield BankDetails(accountName, bic, iban)
    }

  implicit lazy val arbitraryFixedEstablishment: Arbitrary[TradeDetails] =
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
        address     <- arbitrary[InternationalAddress]
      } yield TradeDetails(tradingName, address)
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      Gen.oneOf(Country.euCountries)
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
        line1      <- commonFieldString(35)
        line2      <- Gen.option(commonFieldString(35))
        townOrCity <- commonFieldString(35)
        county     <- Gen.option(commonFieldString(35))
        postCode   <- ukPostcode()
      } yield UkAddress(normaliseSpaces(line1), normaliseSpaces(line2), normaliseSpaces(townOrCity), normaliseSpaces(county), normaliseSpaces(postCode))
    }

  implicit def arbitraryVrn: Arbitrary[Vrn] = Arbitrary {
    for {
      prefix <- Gen.oneOf("", "GB")
      chars  <- Gen.listOfN(9, Gen.numChar)
    } yield {
      Vrn(prefix + chars.mkString(""))
    }
  }

  def ukPostcode(): Gen[String] = (
    for {
      numberOfFirstLetters <- choose(1,2)
      firstLetters <- listOfN(numberOfFirstLetters, Gen.alphaChar)
      firstNumber <- Gen.numChar
      numberOfMiddle <- choose(0,1)
      middle <- listOfN(numberOfMiddle, Gen.alphaNumChar)
      lastNumber <- Gen.numChar
      lastLetters <- listOfN(2, Gen.alphaChar)

    } yield firstLetters.mkString + firstNumber.toString + middle.mkString + lastNumber.toString + lastLetters.mkString
    )

  private def commonFieldString(maxLength: Int): Gen[String] = (for {
    length <- choose(1, maxLength)
    chars  <- listOfN(length, commonFieldSafeInputs)
  } yield chars.mkString).retryUntil(_.trim.nonEmpty)

  private def commonFieldSafeInputs: Gen[Char] = Gen.oneOf(
    Gen.alphaNumChar,
    Gen.oneOf('À' to 'ÿ'),
    Gen.const('.'),
    Gen.const(','),
    Gen.const('/'),
    Gen.const('’'),
    Gen.const('\''),
    Gen.const('"'),
    Gen.const('_'),
    Gen.const('&'),
    Gen.const(' '),
    Gen.const('\'')
  )

  implicit val arbitrarySavedUserAnswers: Arbitrary[SavedUserAnswers] =
    Arbitrary {
      for {
        vrn         <- arbitrary[Vrn]
        data        = JsObject(Seq("test" -> Json.toJson("test")))
        now         = Instant.now
      } yield SavedUserAnswers(vrn, data, None, now)
    }

  implicit val arbitraryPeriod: Arbitrary[Period] =
    Arbitrary {
      for {
        year <- Gen.choose(2022, 2099)
        quarter <- Gen.oneOf(Quarter.values)
      } yield Period(year, quarter)
    }
}
