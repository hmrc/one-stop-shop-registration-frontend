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

package services

import base.SpecBase
import connectors.returns.VatReturnConnector
import controllers.routes
import models.*
import models.domain.*
import models.domain.returns.VatReturn
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.requests.AuthenticatedDataRequest
import models.responses.NotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*
import pages.euDetails.*
import pages.previousRegistrations.*
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import queries.{AllTradingNames, AllWebsites}
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationServiceSpec
  extends SpecBase
    with MockitoSugar
    with ScalaCheckPropertyChecks
    with OptionValues {

  private val answersPartOfVatGroup =
    UserAnswers("12345-credId",
      vatInfo = Some(vatCustomerInfo)
    )
      .set(BusinessBasedInNiPage, true).success.value
      .set(HasMadeSalesPage, true).success.value
      .set(DateOfFirstSalePage, LocalDate.now).success.value
      .set(HasTradingNamePage, true).success.value
      .set(AllTradingNames, List("single", "double")).success.value
      .set(TaxRegisteredInEuPage, true).success.value

      .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(0)), EuConsumerSalesMethod.DispatchWarehouse).success.value
      .set(RegistrationTypePage(Index(0)), RegistrationType.VatNumber).success.value
      .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
      .set(EuSendGoodsTradingNamePage(Index(0)), "French trading name").success.value
      .set(EuSendGoodsAddressPage(Index(0)), InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France"))).success.value

      .set(EuCountryPage(Index(1)), Country("DE", "Germany")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(1)), false).success.value
      .set(VatRegisteredPage(Index(1)), true).success.value
      .set(EuVatNumberPage(Index(1)), "DE123456789").success.value

      .set(EuCountryPage(Index(2)), Country("IE", "Ireland")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(2)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(2)), EuConsumerSalesMethod.DispatchWarehouse).success.value
      .set(RegistrationTypePage(Index(2)), RegistrationType.TaxId).success.value
      .set(EuTaxReferencePage(Index(2)), "IE123456789").success.value
      .set(EuSendGoodsTradingNamePage(Index(2)), "Irish trading name").success.value
      .set(EuSendGoodsAddressPage(Index(2)), InternationalAddress("Line 1", None, "Town", None, None, Country("IE", "Ireland"))).success.value

      .set(EuCountryPage(Index(3)), Country("ES", "Spain")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(3)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(3)), EuConsumerSalesMethod.FixedEstablishment).success.value
      .set(RegistrationTypePage(Index(3)), RegistrationType.VatNumber).success.value
      .set(EuVatNumberPage(Index(3)), "ES123456789").success.value
      .set(FixedEstablishmentTradingNamePage(Index(3)), "Spanish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(3)), InternationalAddress("Line 1", None, "Town", None, None, Country("ES", "Spain"))).success.value

      .set(EuCountryPage(Index(4)), Country("DK", "Denmark")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(4)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(4)), EuConsumerSalesMethod.FixedEstablishment).success.value
      .set(RegistrationTypePage(Index(4)), RegistrationType.TaxId).success.value
      .set(EuTaxReferencePage(Index(4)), "DK123456789").success.value
      .set(FixedEstablishmentTradingNamePage(Index(4)), "Danish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(4)), InternationalAddress("Line 1", None, "Town", None, None, Country("DK", "Denmark"))).success.value

      .set(IsOnlineMarketplacePage, false).success.value
      .set(HasWebsitePage, true).success.value
      .set(AllWebsites, List("website1", "website2")).success.value

      .set(BankDetailsPage, BankDetails("Account name", Some(Bic("ABCDGB2A").get), Iban("GB33BUKB20201555555555").
        getOrElse(throw new Exception("TODO")))).success.value

      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
      .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).success.value

  private val mockDateService: DateService = mock[DateService]
  private val mockPeriodService: PeriodService = mock[PeriodService]
  private val mockVatReturnConnector: VatReturnConnector = mock[VatReturnConnector]
  private val instant = Instant.now
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private lazy val dateOfFirstSaleRoute = routes.DateOfFirstSaleController.onPageLoad(NormalMode).url
  implicit val dataRequest: AuthenticatedDataRequest[AnyContent] =
    AuthenticatedDataRequest(FakeRequest(GET, dateOfFirstSaleRoute), testCredentials, vrn, None, emptyUserAnswers, None, 0, None)

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  ".toUserAnswers" - {

    "normal registration returns good user answers for all pages" in {

      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)

      val originalRegistrationData = RegistrationData.registration
      val modifiedRegistrationData =
        originalRegistrationData.copy(
          euRegistrations = Seq(
            RegistrationWithoutFixedEstablishmentWithTradeDetails(Country("FR", "France"),
              EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("123456789")),
              TradeDetails(
                "French trading name",
                InternationalAddress(
                  line1 = "Line 1",
                  line2 = None,
                  townOrCity = "Town",
                  stateOrRegion = None,
                  None,
                  Country("FR", "France")))),
            EuVatRegistration(Country("DE", "Germany"), "123456789"),
            RegistrationWithoutFixedEstablishmentWithTradeDetails(
              Country("IE", "Ireland"),
              EuTaxIdentifier(EuTaxIdentifierType.Other, Some("IE123456789")),
              TradeDetails(
                "Irish trading name",
                InternationalAddress(
                  line1 = "Line 1",
                  line2 = None,
                  townOrCity = "Town",
                  stateOrRegion = None,
                  None,
                  Country("IE", "Ireland")
                ))
            ),
            RegistrationWithFixedEstablishment(
              Country("ES", "Spain"),
              EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("123456789")),
              TradeDetails("Spanish trading name", InternationalAddress("Line 1", None, "Town", None, None, Country("ES", "Spain")))
            ),
            RegistrationWithFixedEstablishment(
              Country("DK", "Denmark"),
              EuTaxIdentifier(EuTaxIdentifierType.Other, Some("DK123456789")),
              TradeDetails("Danish trading name", InternationalAddress("Line 1", None, "Town", None, None, Country("DK", "Denmark")))
            ),
          )
        )

      val result = service.toUserAnswers(userAnswersId, modifiedRegistrationData, vatCustomerInfo).futureValue

      result mustBe answersPartOfVatGroup.copy(lastUpdated = result.lastUpdated)
    }

  }

  ".eligibleSalesDifference" - {

    "return true if the user answers are different" in {

      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)

      val result = service.eligibleSalesDifference(Some(RegistrationData.registration), completeUserAnswers)

      result mustBe true
    }

    "return true if there is no registration provided" in {

      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)

      val result = service.eligibleSalesDifference(None, completeUserAnswers)

      result mustBe true
    }

    "return false if the user answers are not different" in {

      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)

      val userAnswers = completeUserAnswers.set(DateOfFirstSalePage, RegistrationData.registration.dateOfFirstSale.get).success.value

      val result = service.eligibleSalesDifference(Some(RegistrationData.registration), userAnswers)

      result mustBe false
    }
  }

  ".isEligibleSalesAmendable" - {

    "return true when registrations is amendable" in {

      implicit val dataRequest: AuthenticatedDataRequest[AnyContent] =
        AuthenticatedDataRequest(FakeRequest(GET, dateOfFirstSaleRoute), testCredentials, vrn, Some(RegistrationData.registration), emptyUserAnswers, None, 0, None)
      when(mockVatReturnConnector.get(any())(any())) thenReturn Future.successful(Left(NotFound))
      when(mockPeriodService.getFirstReturnPeriod(any())) thenReturn period
      when(mockDateService.calculateFinalAmendmentDate(any())(any())) thenReturn LocalDate.now(stubClock)
      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)

      val result = service.isEligibleSalesAmendable(any()).futureValue

      result mustBe true
    }

    "return true when no registration provided" in {

      implicit val dataRequest: AuthenticatedDataRequest[AnyContent] =
        AuthenticatedDataRequest(FakeRequest(GET, dateOfFirstSaleRoute), testCredentials, vrn, None, emptyUserAnswers, None, 0, None)
      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)
      val mode = AmendMode

      val result = service.isEligibleSalesAmendable(mode).futureValue

      result mustBe true
    }

    "return false when today is passed the amendable date" in {

      implicit val dataRequest: AuthenticatedDataRequest[AnyContent] =
        AuthenticatedDataRequest(FakeRequest(GET, dateOfFirstSaleRoute), testCredentials, vrn, Some(RegistrationData.registration), emptyUserAnswers, None, 0, None)
      val daysToAdd = 100
      val instant = Instant.now.plus(daysToAdd, ChronoUnit.DAYS)
      val adjustedStubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
      val mode = AmendMode

      when(mockDateService.calculateFinalAmendmentDate(any())(any())) thenReturn LocalDate.now(stubClock)
      when(mockVatReturnConnector.get(any())(any())) thenReturn Future.successful(Left(NotFound))

      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, adjustedStubClock)

      val result = service.isEligibleSalesAmendable(mode).futureValue

      result mustBe false
    }

    "return false when vat return has been submitted not amendable" in {

      implicit val dataRequest: AuthenticatedDataRequest[AnyContent] =
        AuthenticatedDataRequest(FakeRequest(GET, dateOfFirstSaleRoute), testCredentials, vrn, Some(RegistrationData.registration), emptyUserAnswers, None, 0, None)
      val vatReturn = arbitrary[VatReturn].sample.value
      when(mockVatReturnConnector.get(any())(any())) thenReturn Future.successful(Right(vatReturn))
      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)

      val result = service.isEligibleSalesAmendable(any()).futureValue

      result mustBe false
    }

    "return true when in Rejoin Mode" in {

      implicit val dataRequest: AuthenticatedDataRequest[AnyContent] =
        AuthenticatedDataRequest(FakeRequest(GET, dateOfFirstSaleRoute), testCredentials, vrn, None, emptyUserAnswers, None, 0, None)
      val service = new RegistrationService(mockDateService, mockPeriodService, mockVatReturnConnector, stubClock)
      val mode = RejoinMode

      val result = service.isEligibleSalesAmendable(mode).futureValue

      result mustBe true
    }
  }
}
