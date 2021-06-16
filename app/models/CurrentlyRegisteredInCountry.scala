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

package models

import models.CurrentlyRegisteredInCountry.{No, Yes}
import play.api.libs.json._

sealed trait CurrentlyRegisteredInCountry {
  val asBoolean: Boolean
}

object CurrentlyRegisteredInCountry {

  case class Yes(country: Country) extends CurrentlyRegisteredInCountry { override val asBoolean: Boolean = true }
  case object No extends CurrentlyRegisteredInCountry { override val asBoolean: Boolean = false }

  implicit val reads: Reads[CurrentlyRegisteredInCountry] = new Reads[CurrentlyRegisteredInCountry] {
    override def reads(json: JsValue): JsResult[CurrentlyRegisteredInCountry] =
      json match {
        case JsString("no") => JsSuccess(No)
        case o: JsObject => o.validate[Country].map(Yes)
      }
  }

  implicit val writes: Writes[CurrentlyRegisteredInCountry] = new Writes[CurrentlyRegisteredInCountry] {
    override def writes(o: CurrentlyRegisteredInCountry): JsValue = o match {
      case Yes(country) => Json.toJson(country)
      case _            => JsString("no")
    }
  }
}
