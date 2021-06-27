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

package config

import java.time.LocalDate

object Constants {

  val maxTradingNames: Int = 10
  val maxWebsites: Int = 10

  val registrationConfirmationTemplateId = "oss_registration_confirmation"

  val schemeStartDate: LocalDate = LocalDate.of(2021, 7, 1)

  val tradingNameReservedWords = Set("limited", "ltd", "llp", "plc")
}
