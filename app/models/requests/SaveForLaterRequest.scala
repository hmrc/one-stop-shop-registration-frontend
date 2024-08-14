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

package models.requests

import models.UserAnswers
import play.api.libs.json.{Json, JsValue, OFormat}
import uk.gov.hmrc.domain.Vrn


case class SaveForLaterRequest(
                                vrn: Vrn,
                                data: JsValue
                              )

object SaveForLaterRequest {

  implicit val format: OFormat[SaveForLaterRequest] = Json.format[SaveForLaterRequest]

  def apply(answers: UserAnswers, vrn: Vrn): SaveForLaterRequest = SaveForLaterRequest(vrn, answers.data)
}
