/*
 * Copyright 2024 HM Revenue & Customs
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
import models.*
import models.domain.*
import models.domain.ModelHelpers.normaliseSpaces
import models.domain.returns.*
import models.domain.returns.VatOnSalesChoice.Standard
import models.enrolments.{EACDEnrolment, EACDEnrolments, EACDIdentifiers}
import models.etmp.EtmpExclusion
import models.etmp.intermediary.*
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.exclusions.{ExcludedTrader, ExclusionReason}
import models.iossRegistration.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{choose, listOfN}
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import scala.math.BigDecimal.RoundingMode

trait ModelGenerators {

  implicit lazy val arbitraryRegistrationType: Arbitrary[RegistrationType] =
    Arbitrary {
      Gen.oneOf(RegistrationType.values.toSeq)
    }

  implicit lazy val arbitraryEUConsumerSalesMethod: Arbitrary[EuConsumerSalesMethod] =
    Arbitrary {
      Gen.oneOf(EuConsumerSalesMethod.values.toSeq)
    }

  implicit lazy val arbitraryPreviousScheme: Arbitrary[PreviousScheme] =
    Arbitrary {
      Gen.oneOf(PreviousScheme.values.toSeq)
    }

  implicit lazy val arbitraryPreviousSchemeType: Arbitrary[PreviousSchemeType] =
    Arbitrary {
      Gen.oneOf(PreviousSchemeType.values.toSeq)
    }

  implicit lazy val arbitraryPreviousIossSchemeDetails: Arbitrary[PreviousSchemeNumbers] =
    Arbitrary {
      PreviousSchemeNumbers("12345667", Some("test"))
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
        char7 <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9).map(_.toString.head))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9).map(_.toString.head)
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
      ).map(v => Iban(v).toOption.get)
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1 <- commonFieldString(35)
        line2 <- Gen.option(commonFieldString(35))
        townOrCity <- commonFieldString(35)
        stateOrRegion <- Gen.option(commonFieldString(35))
        postCode <- Gen.option(arbitrary[String])
        country <- Gen.oneOf(Country.internationalCountries)
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
        value <- Gen.option(arbitrary[Int].toString)
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
        bic <- Gen.option(arbitrary[Bic])
        iban <- arbitrary[Iban]
      } yield BankDetails(accountName, bic, iban)
    }

  implicit lazy val arbitraryFixedEstablishment: Arbitrary[TradeDetails] =
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
        address <- arbitrary[InternationalAddress]
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
        line1 <- commonFieldString(35)
        line2 <- Gen.option(commonFieldString(35))
        townOrCity <- commonFieldString(35)
        county <- Gen.option(commonFieldString(35))
        postCode <- ukPostcode()
      } yield UkAddress(normaliseSpaces(line1), normaliseSpaces(line2), normaliseSpaces(townOrCity), normaliseSpaces(county), normaliseSpaces(postCode))
    }

  implicit lazy val arbitraryDesAddress: Arbitrary[DesAddress] = {
    Arbitrary {
      for {
        line1 <- commonFieldString(35)
        line2 <- Gen.option(commonFieldString(35))
        line3 <- Gen.option(commonFieldString(35))
        line4 <- Gen.option(commonFieldString(35))
        line5 <- Gen.option(commonFieldString(35))
        postCode <- Gen.option(arbitrary[String])
        country <- Gen.oneOf(Country.internationalCountries.map(_.code))
      } yield DesAddress(
        normaliseSpaces(line1),
        normaliseSpaces(line2),
        normaliseSpaces(line3),
        normaliseSpaces(line4),
        normaliseSpaces(line5),
        normaliseSpaces(postCode),
        country
      )
    }
  }

  implicit def arbitraryVrn: Arbitrary[Vrn] = Arbitrary {
    for {
      chars <- Gen.listOfN(9, Gen.numChar)
    } yield {
      Vrn(chars.mkString(""))
    }
  }

  def ukPostcode(): Gen[String] = (
    for {
      numberOfFirstLetters <- choose(1, 2)
      firstLetters <- listOfN(numberOfFirstLetters, Gen.alphaChar)
      firstNumber <- Gen.numChar
      numberOfMiddle <- choose(0, 1)
      middle <- listOfN(numberOfMiddle, Gen.alphaNumChar)
      lastNumber <- Gen.numChar
      lastLetters <- listOfN(2, Gen.alphaChar)

    } yield firstLetters.mkString + firstNumber.toString + middle.mkString + lastNumber.toString + lastLetters.mkString
    )

  private def commonFieldString(maxLength: Int): Gen[String] = (for {
    length <- choose(1, maxLength)
    chars <- listOfN(length, commonFieldSafeInputs)
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
        vrn <- arbitrary[Vrn]
        data = JsObject(Seq("test" -> Json.toJson("test")))
        now = Instant.now
      } yield SavedUserAnswers(vrn, data, now)
    }

  implicit val arbitraryPeriod: Arbitrary[Period] =
    Arbitrary {
      for {
        year <- Gen.choose(2022, 2099)
        quarter <- Gen.oneOf(Quarter.values)
      } yield Period(year, quarter)
    }

  implicit val arbitraryVatRate: Arbitrary[VatRate] =
    Arbitrary {
      for {
        vatRateType <- Gen.oneOf(VatRateType.values)
        rate <- Gen.choose(BigDecimal(0), BigDecimal(100))
      } yield VatRate(rate.setScale(2, RoundingMode.HALF_EVEN), vatRateType)
    }


  implicit val arbitrarySalesDetails: Arbitrary[SalesDetails] =
    Arbitrary {
      for {
        vatRate <- arbitrary[VatRate]
        taxableAmount <- Gen.choose(BigDecimal(0), BigDecimal(1000000))
        vatAmount <- Gen.choose(BigDecimal(0), BigDecimal(1000000))
      } yield SalesDetails(
        vatRate,
        taxableAmount.setScale(2, RoundingMode.HALF_EVEN),
        VatOnSales(Standard, vatAmount.setScale(2, RoundingMode.HALF_EVEN))
      )
    }

  implicit val arbitrarySalesToCountry: Arbitrary[SalesToCountry] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        number <- Gen.choose(1, 2)
        amounts <- Gen.listOfN(number, arbitrary[SalesDetails])
      } yield SalesToCountry(country, amounts)
    }

  implicit val arbitrarySalesFromEuCountry: Arbitrary[SalesFromEuCountry] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        taxIdentifier <- Gen.option(arbitrary[EuTaxIdentifier])
        number <- Gen.choose(1, 3)
        amounts <- Gen.listOfN(number, arbitrary[SalesToCountry])
      } yield SalesFromEuCountry(country, taxIdentifier, amounts)
    }

  implicit val arbitraryVatReturn: Arbitrary[VatReturn] =
    Arbitrary {
      for {
        vrn <- arbitrary[Vrn]
        period <- arbitrary[Period]
        niSales <- Gen.choose(1, 3)
        euSales <- Gen.choose(1, 3)
        salesFromNi <- Gen.listOfN(niSales, arbitrary[SalesToCountry])
        salesFromEu <- Gen.listOfN(euSales, arbitrary[SalesFromEuCountry])
        now = Instant.now
      } yield VatReturn(vrn, period, ReturnReference(vrn, period), PaymentReference(vrn, period), None, None, salesFromNi, salesFromEu, now, now)
    }

  implicit val arbitraryIossEtmpExclusion: Arbitrary[IossEtmpExclusion] = {
    Arbitrary {
      for {
        exclusionReason <- Gen.oneOf(IossEtmpExclusionReason.values)
        effectiveDate <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
        decisionDate <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
        quarantine <- arbitrary[Boolean]
      } yield IossEtmpExclusion(
        exclusionReason = exclusionReason,
        effectiveDate = effectiveDate,
        decisionDate = decisionDate,
        quarantine = quarantine
      )
    }
  }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  implicit lazy val arbitraryExclusionReason: Arbitrary[ExclusionReason] =
    Arbitrary {
      Gen.oneOf(ExclusionReason.values)
    }

  implicit lazy val arbitraryExcludedTrader: Arbitrary[ExcludedTrader] = {
    Arbitrary {
      for {
        vrn <- arbitraryVrn.arbitrary
        exclusionReason <- arbitraryExclusionReason.arbitrary
        effectiveDate <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
      } yield ExcludedTrader(
        vrn = vrn,
        exclusionReason = exclusionReason,
        effectiveDate = effectiveDate,
        quarantined = false
      )
    }
  }

  implicit lazy val arbitraryIossEtmpTradingName: Arbitrary[IossEtmpTradingName] = {
    Arbitrary {
      for {
        tradingName <- Gen.alphaStr
      } yield IossEtmpTradingName(tradingName)
    }
  }

  implicit lazy val arbitraryIossEtmpBankDetails: Arbitrary[IossEtmpBankDetails] = {
    Arbitrary {
      for {
        accountName <- Gen.alphaStr
        bic <- arbitrary[Bic]
        iban <- arbitrary[Iban]
      } yield IossEtmpBankDetails(accountName, Some(bic), iban)
    }
  }

  implicit lazy val arbitraryIossEtmpDisplaySchemeDetails: Arbitrary[IossEtmpDisplaySchemeDetails] = {
    Arbitrary {
      for {
        contactName <- Gen.alphaStr
        businessTelephoneNumber <- Gen.alphaStr
        businessEmailId <- Gen.alphaStr
      } yield IossEtmpDisplaySchemeDetails(
        contactName = contactName,
        businessTelephoneNumber = businessTelephoneNumber,
        businessEmailId = businessEmailId
      )
    }
  }

  implicit lazy val arbitraryIossEtmpDisplayRegistration: Arbitrary[IossEtmpDisplayRegistration] = {
    Arbitrary {
      for {
        tradingNames <- Gen.listOfN(3, arbitraryIossEtmpTradingName.arbitrary)
        schemeDetails <- arbitraryIossEtmpDisplaySchemeDetails.arbitrary
        bankDetails <- arbitraryIossEtmpBankDetails.arbitrary
        exclusions <- arbitraryIossEtmpExclusion.arbitrary
      } yield IossEtmpDisplayRegistration(
        tradingNames = tradingNames,
        schemeDetails = schemeDetails,
        bankDetails = bankDetails,
        exclusions = Seq(exclusions)
      )
    }
  }

  implicit lazy val arbitraryEACDIdentifiers: Arbitrary[EACDIdentifiers] = {
    Arbitrary {
      for {
        key <- Gen.alphaStr
        value <- Gen.alphaStr
      } yield EACDIdentifiers(
        key = key,
        value = value
      )
    }
  }

  implicit lazy val arbitraryEACDEnrolment: Arbitrary[EACDEnrolment] = {
    Arbitrary {
      for {
        service <- Gen.alphaStr
        state <- Gen.alphaStr
        identifiers <- Gen.listOfN(3, arbitraryEACDIdentifiers.arbitrary)
      } yield EACDEnrolment(
        service = service,
        state = state,
        activationDate = Some(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
        identifiers = identifiers
      )
    }
  }

  implicit lazy val arbitraryEACDEnrolments: Arbitrary[EACDEnrolments] = {
    Arbitrary {
      for {
        enrolments <- Gen.listOfN(3, arbitraryEACDEnrolment.arbitrary)
      } yield EACDEnrolments(
        enrolments = enrolments
      )
    }
  }

  implicit lazy val arbitraryEtmpCustomerIdentification: Arbitrary[EtmpCustomerIdentificationNew] = {
    Arbitrary {
      for {
        etmpIdType <- Gen.oneOf(EtmpIdType.values)
        vrn <- Gen.alphaStr
      } yield EtmpCustomerIdentificationNew(etmpIdType, vrn)
    }
  }

  implicit lazy val arbitraryEtmpTradingName: Arbitrary[EtmpTradingName] = {
    Arbitrary {
      for {
        tradingName <- Gen.alphaStr
      } yield EtmpTradingName(tradingName)
    }
  }

  implicit lazy val arbitraryEtmpClientDetails: Arbitrary[EtmpClientDetails] = {
    Arbitrary {
      for {
        clientName <- Gen.alphaStr
        clientIossID <- Gen.alphaNumStr
        clientExcluded <- arbitrary[Boolean]
      } yield {
        EtmpClientDetails(
          clientName = clientName,
          clientIossID = clientIossID,
          clientExcluded = clientExcluded
        )
      }
    }
  }

  implicit lazy val genIntermediaryNumber: Gen[String] = {
    for {
      intermediaryNumber <- Gen.listOfN(12, Gen.alphaChar).map(_.mkString)
    } yield intermediaryNumber
  }

  implicit lazy val arbitraryOtherIossIntermediaryRegistrations: Arbitrary[EtmpOtherIossIntermediaryRegistrations] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        intermediaryNumber <- genIntermediaryNumber
      } yield {
        EtmpOtherIossIntermediaryRegistrations(
          issuedBy = issuedBy,
          intermediaryNumber = intermediaryNumber
        )
      }
    }
  }

  implicit lazy val arbitraryIntermediaryDetails: Arbitrary[EtmpIntermediaryDetails] = {
    Arbitrary {
      for {
        otherIossIntermediaryRegistrations <- Gen.listOfN(2, arbitraryOtherIossIntermediaryRegistrations.arbitrary)
      } yield {
        EtmpIntermediaryDetails(
          otherIossIntermediaryRegistrations = otherIossIntermediaryRegistrations
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpOtherAddress: Arbitrary[EtmpIntermediaryOtherAddress] = {
    Arbitrary {
      for {
        issuedBy <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
        tradingName <- Gen.listOfN(20, Gen.alphaChar).map(_.mkString)
        addressLine1 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        addressLine2 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        townOrCity <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        regionOrState <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        postcode <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
      } yield EtmpIntermediaryOtherAddress(
        issuedBy,
        Some(tradingName),
        addressLine1,
        Some(addressLine2),
        townOrCity,
        Some(regionOrState),
        Some(postcode)
      )
    }
  }

  implicit lazy val genEuTaxReference: Gen[String] = {
    Gen.listOfN(20, Gen.alphaNumChar).map(_.mkString)
  }

  implicit lazy val arbitraryEuVatNumber: Gen[String] = {
    for {
      vatNumber <- Gen.alphaNumStr
    } yield vatNumber
  }

  implicit lazy val arbitraryEtmpDisplayEuRegistrationDetails: Arbitrary[EtmpDisplayEuRegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        vatNumber <- arbitraryEuVatNumber
        taxIdentificationNumber <- genEuTaxReference
        fixedEstablishmentTradingName <- arbitraryEtmpTradingName.arbitrary.map(_.tradingName)
        fixedEstablishmentAddressLine1 <- Gen.alphaStr
        fixedEstablishmentAddressLine2 <- Gen.alphaStr
        townOrCity <- Gen.alphaStr
        regionOrState <- Gen.alphaStr
        postcode <- Gen.alphaStr
      } yield {
        EtmpDisplayEuRegistrationDetails(
          issuedBy = issuedBy,
          vatNumber = Some(vatNumber),
          taxIdentificationNumber = Some(taxIdentificationNumber),
          fixedEstablishmentTradingName = fixedEstablishmentTradingName,
          fixedEstablishmentAddressLine1 = fixedEstablishmentAddressLine1,
          fixedEstablishmentAddressLine2 = Some(fixedEstablishmentAddressLine2),
          townOrCity = townOrCity,
          regionOrState = Some(regionOrState),
          postcode = Some(postcode)
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpDisplaySchemeDetails: Arbitrary[EtmpIntermediaryDisplaySchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[LocalDate].map(_.toString)
        euRegistrationDetails <- Gen.listOfN(3, arbitraryEtmpDisplayEuRegistrationDetails.arbitrary)
        contactName <- Gen.alphaStr
        businessTelephoneNumber <- Gen.alphaNumStr
        businessEmailId <- Gen.alphaStr
        unusableStatus <- arbitrary[Boolean]
        nonCompliant <- Gen.oneOf("1", "2")
      } yield {
        EtmpIntermediaryDisplaySchemeDetails(
          commencementDate = commencementDate,
          euRegistrationDetails = euRegistrationDetails,
          contactName = contactName,
          businessTelephoneNumber = businessTelephoneNumber,
          businessEmailId = businessEmailId,
          unusableStatus = unusableStatus,
          nonCompliantReturns = Some(nonCompliant),
          nonCompliantPayments = Some(nonCompliant)
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpExclusion: Arbitrary[EtmpExclusion] = {
    Arbitrary {
      for {
        exclusionReason <- Gen.oneOf(ExclusionReason.values)
        effectiveDate <- arbitrary[LocalDate]
        decisionDate <- arbitrary[LocalDate]
        quarantine <- arbitrary[Boolean]
      } yield {
        EtmpExclusion(
          exclusionReason = exclusionReason,
          effectiveDate = effectiveDate,
          decisionDate = decisionDate,
          quarantine = quarantine
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpAdminUse: Arbitrary[AdminUse] = {
    Arbitrary {
      for {
        changeDate <- arbitrary[LocalDateTime]
      } yield AdminUse(changeDate = Some(changeDate))
    }
  }

  implicit lazy val arbitraryEtmpDisplayRegistration: Arbitrary[EtmpIntermediaryDisplayRegistration] = {
    Arbitrary {
      for {
        customerIdentification <- arbitraryEtmpCustomerIdentification.arbitrary
        tradingNames <- Gen.listOfN(3, arbitraryEtmpTradingName.arbitrary)
        clientDetails <- Gen.listOfN(3, arbitraryEtmpClientDetails.arbitrary)
        intermediaryDetails <- arbitraryIntermediaryDetails.arbitrary
        otherAddress <- arbitraryEtmpOtherAddress.arbitrary
        schemeDetails <- arbitraryEtmpDisplaySchemeDetails.arbitrary
        exclusions <- Gen.listOfN(1, arbitraryEtmpExclusion.arbitrary)
        bankDetails <- arbitraryBankDetails.arbitrary
        adminUse <- arbitraryEtmpAdminUse.arbitrary
      } yield {
        EtmpIntermediaryDisplayRegistration(
          customerIdentification = customerIdentification,
          tradingNames = tradingNames,
          clientDetails = clientDetails,
          intermediaryDetails = Some(intermediaryDetails),
          otherAddress = Some(otherAddress),
          schemeDetails = schemeDetails,
          exclusions = exclusions,
          bankDetails = bankDetails,
          adminUse = adminUse
        )
      }
    }
  }

  implicit lazy val arbitraryIntermediaryDate: Arbitrary[LocalDate] = {
    Arbitrary {
      datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2023, 12, 31))
    }
  }

  implicit val arbitraryVatCustomerInfo: Arbitrary[IntermediaryVatCustomerInfo] = {
    Arbitrary {
      for {
        desAddress <- arbitraryDesAddress.arbitrary
        registrationDate <- arbitraryIntermediaryDate.arbitrary
        organisationName <- Gen.alphaStr
        individualName <- Gen.alphaStr
        singleMarketIndicator <- arbitrary[Boolean]
      } yield {
        IntermediaryVatCustomerInfo(
          desAddress = desAddress,
          registrationDate = Some(registrationDate),
          organisationName = Some(organisationName),
          individualName = Some(individualName),
          singleMarketIndicator = singleMarketIndicator,
          deregistrationDecisionDate = None
        )
      }
    }
  }
}
