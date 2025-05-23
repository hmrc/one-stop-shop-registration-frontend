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

package base

import controllers.actions.*
import generators.Generators
import models.domain.{Registration, VatCustomerInfo}
import models.emailVerification.{EmailVerificationRequest, VerifyEmail}
import models.iossRegistration.IossEtmpDisplayRegistration
import models.requests.AuthenticatedDataRequest
import models.{BankDetails, BusinessContactDetails, Country, DesAddress, Iban, Index, Mode, Period, Quarter, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*
import pages.euDetails.{EuCountryPage, TaxRegisteredInEuPage}
import pages.previousRegistrations.PreviouslyRegisteredPage
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import queries.{AllEuOptionalDetailsQuery, AllTradingNames, AllWebsites}
import services.{DateService, RegistrationService}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.checkAnswers.*
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.{PreviousRegistrationSummary, PreviouslyRegisteredSummary}
import viewmodels.govuk.summarylist.*

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with EitherValues
    with ScalaFutures
    with IntegrationPatience
    with Generators {

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val arbitraryDate: LocalDate = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31)).sample.value
  val arbitraryStartDate: LocalDate = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.now()).sample.value
  val arbitraryInstant: Instant = arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant
  val stubClockAtArbitraryDate: Clock = Clock.fixed(arbitraryInstant, ZoneId.systemDefault)

  def period: Period = Period(2021, Quarter.Q3)

  val userAnswersId: String = "12345-credId"
  val contactDetails: BusinessContactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")

  val vatCustomerInfo: VatCustomerInfo =
    VatCustomerInfo(
      registrationDate = LocalDate.now(stubClockAtArbitraryDate),
      address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      partOfVatGroup = false,
      organisationName = Some("Company name"),
      singleMarketIndicator = Some(true),
      individualName = None,
      deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusYears(10))
    )

  val verifyEmail: VerifyEmail = VerifyEmail(
    address = contactDetails.emailAddress,
    enterUrl = "/pay-vat-on-goods-sold-to-eu/northern-ireland-register/business-contact-details"
  )

  val emailVerificationRequest: EmailVerificationRequest = EmailVerificationRequest(
    credId = userAnswersId,
    continueUrl = "/pay-vat-on-goods-sold-to-eu/northern-ireland-register/bank-details",
    origin = "OSS",
    deskproServiceName = Some("one-stop-shop-registration-frontend"),
    accessibilityStatementUrl = "/register-and-pay-vat-on-goods-sold-to-eu-from-northern-ireland",
    pageTitle = Some("Register to pay VAT on distance sales of goods from Northern Ireland to the EU"),
    backUrl = Some("/pay-vat-on-goods-sold-to-eu/northern-ireland-register/business-contact-details"),
    email = Some(verifyEmail)
  )

  val testCredentials: Credentials = Credentials(userAnswersId, "GGW")
  val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, lastUpdated = arbitraryInstant)
  val basicUserAnswersWithVatInfo: UserAnswers = emptyUserAnswers.set(RegisteredForOssInEuPage, false).success.value copy (vatInfo = Some(vatCustomerInfo))
  val emptyUserAnswersWithVatInfo: UserAnswers = emptyUserAnswers copy (vatInfo = Some(vatCustomerInfo))
  val completeUserAnswers: UserAnswers = basicUserAnswersWithVatInfo
    .set(HasTradingNamePage, false).success.value
    .set(HasMadeSalesPage, false).success.value
    .set(TaxRegisteredInEuPage, false).success.value
    .set(PreviouslyRegisteredPage, false).success.value
    .set(IsOnlineMarketplacePage, false).success.value
    .set(HasWebsitePage, false).success.value
    .set(BusinessContactDetailsPage, BusinessContactDetails("fullname", "123456789", "unittest@email.com")).success.value
    .set(BankDetailsPage, BankDetails("unit test account name", None, Iban("GB33BUKB20201555555555").value)).success.value
  val invalidUserAnswers: UserAnswers = completeUserAnswers
    .set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(Index(0)), Country("Belgium", "BE")).success.value
  val vrn: Vrn = Vrn("123456789")
  val iossNumber: String = "IM9001234567"

  val yourAccountUrl = "http://localhost:10204/pay-vat-on-goods-sold-to-eu/northern-ireland-returns-payments/"

  protected def applicationBuilder(
                                    userAnswers: Option[UserAnswers] = None,
                                    clock: Option[Clock] = None,
                                    mode: Option[Mode] = None,
                                    registration: Option[Registration] = None,
                                    iossNumber: Option[String] = None,
                                    numberOfIossRegistrations: Int = 0,
                                    iossEtmpDisplayRegistration: Option[IossEtmpDisplayRegistration] = None
                                  ): GuiceApplicationBuilder = {

    val clockToBind = clock.getOrElse(stubClockAtArbitraryDate)

    new GuiceApplicationBuilder()
      .overrides(
        bind[AuthenticatedIdentifierAction].toInstance(new FakeAuthenticatedIdentifierAction(registration, iossNumber, numberOfIossRegistrations, iossEtmpDisplayRegistration)),
        bind[AuthenticatedDataRetrievalAction].toInstance(new FakeAuthenticatedDataRetrievalAction(userAnswers, vrn)),
        bind[SavedAnswersRetrievalAction].toInstance(new FakeSavedAnswersRetrievalAction(userAnswers, vrn)),
        bind[UnauthenticatedDataRetrievalAction].toInstance(new FakeUnauthenticatedDataRetrievalAction(userAnswers)),
        bind[CheckRegistrationFilterProvider].toInstance(new FakeCheckRegistrationFilterProvider()),
        bind[CheckNiProtocolFilterImpl].toInstance(new FakeCheckNiProtocolFilterImpl()),
        bind[CheckEmailVerificationFilterProvider].toInstance(new FakeCheckEmailVerificationFilter()),
        bind[CheckOtherCountryRegistrationFilter].toInstance(new FakeCheckOtherCountryRegistrationFilter()),
        bind[CheckRejoinOtherCountryRegistrationFilter].toInstance(new FakeCheckRejoinOtherCountryRegistrationFilter()),
        bind[AuthenticatedDataRequiredAction].toInstance(new FakeAuthenticatedDataRequiredActionProvider(userAnswers, registration)),
        bind[Clock].toInstance(clockToBind)
      )
  }

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "/endpoint").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def getCYAVatRegistrationDetailsSummaryList(answers: UserAnswers)(implicit msgs: Messages): Seq[SummaryListRow] = {
    Seq(
      VatRegistrationDetailsSummary.rowBusinessName(answers),
      VatRegistrationDetailsSummary.rowPartOfVatUkGroup(answers),
      VatRegistrationDetailsSummary.rowUkVatRegistrationDate(answers),
      VatRegistrationDetailsSummary.rowBusinessAddress(answers)
    ).flatten
  }

  def getCYASummaryList(answers: UserAnswers, dateService: DateService, registrationService: RegistrationService, mode: Mode)
                       (implicit msgs: Messages, hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Seq[SummaryListRow]] = {
    new CommencementDateSummary(dateService, registrationService).row(answers).map { commencementDateSummary =>

      val hasTradingNameSummaryRow = new HasTradingNameSummary().row(answers, mode)
      val tradingNameSummaryRow = TradingNameSummary.checkAnswersRow(answers, mode)
      val hasMadeSalesSummaryRow = HasMadeSalesSummary.row(answers, mode)
      val commencementDateSummaryRow = commencementDateSummary
      val previouslyRegisteredSummaryRow = PreviouslyRegisteredSummary.row(answers, mode)
      val previousRegistrationSummaryRow = PreviousRegistrationSummary.checkAnswersRow(answers, Seq.empty, mode)
      val taxRegisteredInEuSummaryRow = TaxRegisteredInEuSummary.row(answers, mode)
      val euDetailsSummaryRow = EuDetailsSummary.checkAnswersRow(answers, mode)
      val isOnlineMarketplaceSummaryRow = IsOnlineMarketplaceSummary.row(answers, mode)
      val hasWebsiteSummaryRow = HasWebsiteSummary.row(answers, mode)
      val websiteSummaryRow = WebsiteSummary.checkAnswersRow(answers, mode)
      val businessContactDetailsContactNameSummaryRow = BusinessContactDetailsSummary.rowContactName(answers, mode)
      val businessContactDetailsTelephoneSummaryRow = BusinessContactDetailsSummary.rowTelephoneNumber(answers, mode)
      val businessContactDetailsEmailSummaryRow = BusinessContactDetailsSummary.rowEmailAddress(answers, mode)
      val bankDetailsAccountNameSummaryRow = BankDetailsSummary.rowAccountName(answers, mode)
      val bankDetailsBicSummaryRow = BankDetailsSummary.rowBIC(answers, mode)
      val bankDetailsIbanSummaryRow = BankDetailsSummary.rowIBAN(answers, mode)

      Seq(
        hasTradingNameSummaryRow.map { sr =>
          if (tradingNameSummaryRow.isDefined) {
            sr.withCssClass("govuk-summary-list__row--no-border")
          } else {
            sr
          }
        },
        tradingNameSummaryRow,
        hasMadeSalesSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
        commencementDateSummaryRow,
        previouslyRegisteredSummaryRow.map { sr =>
          if (previousRegistrationSummaryRow.isDefined) {
            sr.withCssClass("govuk-summary-list__row--no-border")
          } else {
            sr
          }
        },
        previousRegistrationSummaryRow,
        taxRegisteredInEuSummaryRow.map { sr =>
          if (euDetailsSummaryRow.isDefined) {
            sr.withCssClass("govuk-summary-list__row--no-border")
          } else {
            sr
          }
        },
        euDetailsSummaryRow,
        isOnlineMarketplaceSummaryRow,
        hasWebsiteSummaryRow.map { sr =>
          if (websiteSummaryRow.isDefined) {
            sr.withCssClass("govuk-summary-list__row--no-border")
          } else {
            sr
          }
        },
        websiteSummaryRow,
        businessContactDetailsContactNameSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
        businessContactDetailsTelephoneSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
        businessContactDetailsEmailSummaryRow,
        bankDetailsAccountNameSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
        bankDetailsBicSummaryRow.map(_.withCssClass("govuk-summary-list__row--no-border")),
        bankDetailsIbanSummaryRow
      ).flatten
    }
  }

  def getAmendedCYASummaryList(answers: UserAnswers, dateService: DateService, registrationService: RegistrationService, registration: Option[Registration])
                              (implicit msgs: Messages, hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Seq[SummaryListRow]] = {
    new CommencementDateSummary(dateService, registrationService).row(answers).map { commencementDateSummary =>

      val hasTradingNameSummaryRow = new HasTradingNameSummary().amendedAnswersRow(answers)
      val tradingNameSummaryRow = TradingNameSummary.amendedAnswersRow(answers)
      val removedTradingNameRows = TradingNameSummary.removedAnswersRow(getRemovedTradingNames(answers, registration))
      val hasMadeSalesSummaryRow = HasMadeSalesSummary.amendedAnswersRow(answers)
      val dateOfFirstSaleSummary = DateOfFirstSaleSummary.amendedAnswersRow(request.userAnswers)
      val commencementDateSummaryRow = commencementDateSummary
      val taxRegisteredInEuSummaryRow = TaxRegisteredInEuSummary.amendedAnswersRow(answers)
      val euDetailsSummaryRow = EuDetailsSummary.amendedAnswersRow(answers)
      val removedEuDetailsRow = EuDetailsSummary.removedAnswersRow(getRemovedEuDetails(answers, registration))
      val isOnlineMarketplaceSummaryRow = IsOnlineMarketplaceSummary.amendedAnswerRow(answers)
      val hasWebsiteSummaryRow = HasWebsiteSummary.amendedAnswersRow(answers)
      val websiteSummaryRow = WebsiteSummary.amendedAnswersRow(answers)
      val removedWebsiteRow = WebsiteSummary.removedWebsiteRow(getRemovedWebsites(answers, registration))
      val businessContactDetailsContactNameSummaryRow = BusinessContactDetailsSummary.amendedContactNameRow(answers)
      val businessContactDetailsTelephoneSummaryRow = BusinessContactDetailsSummary.amendedTelephoneNumberRow(answers)
      val businessContactDetailsEmailSummaryRow = BusinessContactDetailsSummary.amendedEmailAddressRow(answers)
      val bankDetailsAccountNameSummaryRow = BankDetailsSummary.amendedAccountNameRow(answers)
      val bankDetailsBicSummaryRow = BankDetailsSummary.amendedBICRow(answers)
      val bankDetailsIbanSummaryRow = BankDetailsSummary.amendedIBANRow(answers)

      Seq(
        hasTradingNameSummaryRow,
        tradingNameSummaryRow,
        removedTradingNameRows,
        hasMadeSalesSummaryRow,
        dateOfFirstSaleSummary,
        commencementDateSummaryRow,
        taxRegisteredInEuSummaryRow,
        euDetailsSummaryRow,
        removedEuDetailsRow,
        isOnlineMarketplaceSummaryRow,
        hasWebsiteSummaryRow,
        websiteSummaryRow,
        removedWebsiteRow,
        businessContactDetailsContactNameSummaryRow,
        businessContactDetailsTelephoneSummaryRow,
        businessContactDetailsEmailSummaryRow,
        bankDetailsAccountNameSummaryRow,
        bankDetailsBicSummaryRow,
        bankDetailsIbanSummaryRow
      ).flatten
    }
  }

  private def getRemovedTradingNames(answers: UserAnswers, registration: Option[Registration]): Seq[String] = {
    val amendedAnswers = answers.get(AllTradingNames).getOrElse(List.empty)
    val originalAnswers = registration.map(_.tradingNames).getOrElse(List.empty)

    originalAnswers.diff(amendedAnswers)
  }

  private def getRemovedEuDetails(answers: UserAnswers, registration: Option[Registration]): Seq[Country] = {
    val amendedAnswers = answers.get(AllEuOptionalDetailsQuery).map(_.map(_.euCountry)).getOrElse(List.empty)
    val originalAnswers = registration.map(_.euRegistrations.map(_.country)).getOrElse(List.empty)

    originalAnswers.diff(amendedAnswers)
  }

  private def getRemovedWebsites(answers: UserAnswers, registration: Option[Registration]): Seq[String] = {
    val amendedAnswers = answers.get(AllWebsites).getOrElse(List.empty)
    val originalAnswers = registration.map(_.websites).getOrElse(List.empty)

    originalAnswers.diff(amendedAnswers)
  }

}
