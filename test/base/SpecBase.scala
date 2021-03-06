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

package base

import controllers.actions._
import generators.Generators
import models.domain.VatCustomerInfo
import models.{Country, DesAddress, Index, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.euDetails.{EuCountryPage, TaxRegisteredInEuPage}
import pages.previousRegistrations.PreviouslyRegisteredPage
import pages._
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
import viewmodels.checkAnswers.euDetails.TaxRegisteredInEuSummary
import viewmodels.checkAnswers.previousRegistrations.PreviouslyRegisteredSummary
import viewmodels.checkAnswers._

import java.time.{Clock, Instant, LocalDate, ZoneId}

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with Generators {

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val arbitraryDate: LocalDate        = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31)).sample.value
  val arbitraryStartDate: LocalDate   = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.now()).sample.value
  val arbitraryInstant: Instant       = arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant
  val stubClockAtArbitraryDate: Clock = Clock.fixed(arbitraryInstant, ZoneId.systemDefault)

  val userAnswersId: String = "12345-credId"

  val vatCustomerInfo: VatCustomerInfo =
    VatCustomerInfo(
      registrationDate = Some(LocalDate.now(stubClockAtArbitraryDate)),
      address          = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      partOfVatGroup   = Some(true),
      organisationName = Some("Company name")
    )

  val partialVatCustomerInfo: VatCustomerInfo =
    VatCustomerInfo(
      registrationDate = None,
      address          = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      partOfVatGroup   = None,
      organisationName = None
    )

  val testCredentials: Credentials             = Credentials(userAnswersId, "GGW")
  val emptyUserAnswers: UserAnswers            = UserAnswers(userAnswersId, lastUpdated = arbitraryInstant)
  val basicUserAnswers: UserAnswers = emptyUserAnswers.set(RegisteredForOssInEuPage, false).success.value
  val emptyUserAnswersWithVatInfo: UserAnswers = emptyUserAnswers copy (vatInfo = Some(vatCustomerInfo))
  val basicUserAnswersWithVatInfo: UserAnswers = basicUserAnswers copy (vatInfo = Some(vatCustomerInfo))
  val partialUserAnswersWithVatInfo: UserAnswers = emptyUserAnswers copy (vatInfo = Some(partialVatCustomerInfo))
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
        bind[CheckRegistrationFilter].toInstance(new FakeCheckRegistrationFilter()),
        bind[CheckNiProtocolFilter].toInstance(new FakeCheckNiProtocolFilter()),
        bind[Clock].toInstance(clockToBind)
      )
  }

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def getCYASummaryList(answers: UserAnswers, dateService: DateService)(implicit msgs: Messages) ={
    Seq(
      RegisteredCompanyNameSummary.row(answers),
      PartOfVatGroupSummary.row(answers),
      UkVatEffectiveDateSummary.row(answers),
      BusinessAddressInUkSummary.row(answers),
      UkAddressSummary.row(answers),
      InternationalAddressSummary.row(answers),
      new HasTradingNameSummary().row(answers),
      HasMadeSalesSummary.row(answers),
      IsPlanningFirstEligibleSaleSummary.row(answers),
      new CommencementDateSummary(dateService).row(answers),
      TaxRegisteredInEuSummary.row(answers),
      PreviouslyRegisteredSummary.row(answers),
      IsOnlineMarketplaceSummary.row(answers),
      HasWebsiteSummary.row(answers),
      BusinessContactDetailsSummary.row(answers),
      BankDetailsSummary.row(answers)
    ).flatten
  }

}
