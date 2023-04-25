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

package base

import controllers.actions._
import generators.Generators
import models.domain.VatCustomerInfo
import models.emailVerification.{EmailVerificationRequest, VerifyEmail}
import models.requests.AuthenticatedDataRequest
import models.{BusinessContactDetails, Country, DesAddress, Index, Mode, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages._
import pages.euDetails.{EuCountryPage, TaxRegisteredInEuPage}
import pages.previousRegistrations.PreviouslyRegisteredPage
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import services.DateService
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.checkAnswers._
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers.previousRegistrations.PreviouslyRegisteredSummary
import viewmodels.govuk.summarylist._

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with Generators {

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val arbitraryDate: LocalDate = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31)).sample.value
  val arbitraryStartDate: LocalDate = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.now()).sample.value
  val arbitraryInstant: Instant = arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant
  val stubClockAtArbitraryDate: Clock = Clock.fixed(arbitraryInstant, ZoneId.systemDefault)

  val userAnswersId: String = "12345-credId"
  val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")

  val vatCustomerInfo: VatCustomerInfo =
    VatCustomerInfo(
      registrationDate = LocalDate.now(stubClockAtArbitraryDate),
      address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      partOfVatGroup = false,
      organisationName      = Some("Company name"),
      singleMarketIndicator =  Some(true),
      individualName = None
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
    .set(IsPlanningFirstEligibleSalePage, true).success.value
    .set(TaxRegisteredInEuPage, false).success.value
    .set(PreviouslyRegisteredPage, false).success.value
    .set(IsOnlineMarketplacePage, false).success.value
    .set(HasWebsitePage, false).success.value
  val invalidUserAnswers = completeUserAnswers
    .set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(Index(0)), Country("Belgium", "BE")).success.value
  val vrn: Vrn = Vrn("123456789")

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None, clock: Option[Clock] = None): GuiceApplicationBuilder = {

    val clockToBind = clock.getOrElse(stubClockAtArbitraryDate)

    new GuiceApplicationBuilder()
      .overrides(
        bind[AuthenticatedIdentifierAction].to[FakeAuthenticatedIdentifierAction],
        bind[AuthenticatedDataRetrievalAction].toInstance(new FakeAuthenticatedDataRetrievalAction(userAnswers, vrn)),
        bind[SavedAnswersRetrievalAction].toInstance(new FakeSavedAnswersRetrievalAction(userAnswers, vrn)),
        bind[UnauthenticatedDataRetrievalAction].toInstance(new FakeUnauthenticatedDataRetrievalAction(userAnswers, vrn)),
        bind[CheckRegistrationFilterProvider].toInstance(new FakeCheckRegistrationFilterProvider()),
        bind[CheckNiProtocolFilter].toInstance(new FakeCheckNiProtocolFilter()),
        bind[CheckEmailVerificationFilter].toInstance(new FakeCheckEmailVerificationFilter()),
        bind[CheckOtherCountryRegistrationFilter].toInstance(new FakeCheckOtherCountryRegistrationFilter()),
        bind[Clock].toInstance(clockToBind)
      )
  }

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def getCYAVatRegistrationDetailsSummaryList(answers: UserAnswers)(implicit msgs: Messages) = {
    Seq(
      VatRegistrationDetailsSummary.rowBusinessName(answers),
      VatRegistrationDetailsSummary.rowPartOfVatUkGroup(answers),
      VatRegistrationDetailsSummary.rowUkVatRegistrationDate(answers),
      VatRegistrationDetailsSummary.rowBusinessAddress(answers)
    ).flatten
  }

  def getCYASummaryList(answers: UserAnswers, dateService: DateService, mode: Mode)
                       (implicit msgs: Messages, hc: HeaderCarrier, request: AuthenticatedDataRequest[_]) = {
    new CommencementDateSummary(dateService).row(answers).map { commencementDateSummary =>

      Seq(
        new HasTradingNameSummary().row(answers, mode).map(_.withCssClass("govuk-summary-list__row--no-border")),
        HasMadeSalesSummary.row(answers).map(_.withCssClass("govuk-summary-list__row--no-border")),
        IsPlanningFirstEligibleSaleSummary.row(answers).map(_.withCssClass("govuk-summary-list__row--no-border")),
        Some(commencementDateSummary),
        PreviouslyRegisteredSummary.row(answers).map(_.withCssClass("govuk-summary-list__row--no-border")),
        TaxRegisteredInEuSummary.row(answers).map(_.withCssClass("govuk-summary-list__row--no-border")),
        EuDetailsSummary.checkAnswersRow(answers),
        IsOnlineMarketplaceSummary.row(answers, mode),
        HasWebsiteSummary.row(answers, mode).map(_.withCssClass("govuk-summary-list__row--no-border")),
        BusinessContactDetailsSummary.rowContactName(answers).map(_.withCssClass("govuk-summary-list__row--no-border")),
        BusinessContactDetailsSummary.rowTelephoneNumber(answers).map(_.withCssClass("govuk-summary-list__row--no-border")),
        BusinessContactDetailsSummary.rowEmailAddress(answers),
        BankDetailsSummary.rowAccountName(answers, mode).map(_.withCssClass("govuk-summary-list__row--no-border")),
        BankDetailsSummary.rowBIC(answers, mode).map(_.withCssClass("govuk-summary-list__row--no-border")),
        BankDetailsSummary.rowIBAN(answers, mode)
      ).flatten
    }
  }

}
