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

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait AlreadyMadeSales

object AlreadyMadeSales {

  case object No extends AlreadyMadeSales

  case class Yes(firstSale: LocalDate) extends AlreadyMadeSales

  object Yes {

    val reads: Reads[Yes] =
      (__ \ "answer").read[Boolean].flatMap[Boolean] {
        a =>
          if (a == true) Reads(_ => JsSuccess(a)) else Reads(_ => JsError("answer must be true"))
      }.andKeep(
        (__ \ "firstSale").read[LocalDate]
      ).map(Yes(_))

    val writes: Writes[Yes] = new Writes[Yes] {
      override def writes(o: Yes): JsValue =
        Json.obj(
          "answer" -> true,
          "firstSale" -> Json.toJson(o.firstSale)
        )
    }
  }

  implicit val reads: Reads[AlreadyMadeSales] = new Reads[AlreadyMadeSales] {

    override def reads(json: JsValue): JsResult[AlreadyMadeSales] = json match {
      case JsString("no") => JsSuccess(No)
      case obj            => obj.validate[Yes](Yes.reads)
    }
  }

  implicit val writes: Writes[AlreadyMadeSales] = new Writes[AlreadyMadeSales] {

    override def writes(o: AlreadyMadeSales): JsValue = o match {
      case No     => JsString("no")
      case y: Yes => Json.toJson(y)(Yes.writes)
    }
  }
}

