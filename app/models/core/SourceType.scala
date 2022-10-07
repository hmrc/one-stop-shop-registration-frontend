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

package models.core

import models.{Enumerable, WithName}

sealed trait SourceType

object SourceType extends Enumerable.Implicits {
  case object VATNumber extends WithName("VATNumber") with SourceType
  case object EUVATNumber extends WithName("EUVATNumber") with SourceType
  case object EUTraderId extends WithName("EUTraderId") with SourceType
  case object TraderId extends WithName("TraderId") with SourceType

  val values: Seq[SourceType] = Seq(
    VATNumber,
    EUVATNumber,
    EUTraderId,
    TraderId
  )

  implicit val enumerable: Enumerable[SourceType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
