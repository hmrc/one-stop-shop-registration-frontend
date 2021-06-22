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

package services

import java.time.LocalDate
import play.api.Configuration
import uk.gov.hmrc.domain.Vrn

import java.time.Clock
import javax.inject.Inject

class FeatureFlagService @Inject()(configuration: Configuration, clock: Clock) {

  val schemeStartDate = LocalDate.of(2021, 7, 1)

  def schemeHasStarted: Boolean = LocalDate.now(clock) isBefore schemeStartDate

  val proceedWhenVatApiCallFails: Boolean = configuration.get[Boolean]("features.proceed-when-vat-api-call-fails")

  val restrictAccessUsingVrnAllowList: Boolean = configuration.get[Boolean]("features.restrict-access-using-vrn-allow-list")
  val vrnAllowList: Seq[Vrn]                   = configuration.get[Seq[String]]("features.vrn-allow-list").map(Vrn(_))
  val vrnBlockedRedirectUrl: String            = configuration.get[String]("features.vrn-blocked-redirect-url")
}
