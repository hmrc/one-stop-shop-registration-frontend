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

package base

import controllers.actions._
import generators.Generators
import models.domain.VatCustomerInfo
import models.{DesAddress, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn

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
  val arbitraryInstant: Instant       = arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant
  val stubClockAtArbitraryDate: Clock = Clock.fixed(arbitraryInstant, ZoneId.systemDefault)

  val userAnswersId: String = "id"

  val vatCustomerInfo: VatCustomerInfo =
    VatCustomerInfo(
      registrationDate = Some(LocalDate.now(stubClockAtArbitraryDate)),
      address          = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      partOfVatGroup   = Some(true),
      organisationName = Some("Company name")
    )

  val emptyUserAnswers: UserAnswers            = UserAnswers(userAnswersId, lastUpdated = arbitraryInstant)
  val emptyUserAnswersWithVatInfo: UserAnswers = emptyUserAnswers copy (vatInfo = Some(vatCustomerInfo))

  val vrn: Vrn = Vrn("123456789")

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, vrn)),
        bind[CheckRegistrationFilter].toInstance(new FakeCheckRegistrationFilter()),
        bind[Clock].toInstance(stubClockAtArbitraryDate)
      )

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

}
