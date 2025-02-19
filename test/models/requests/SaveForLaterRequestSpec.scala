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

import base.SpecBase
import play.api.libs.json.{JsError, Json}

class SaveForLaterRequestSpec extends SpecBase {

  "SaveForLaterRequest" - {

    "serialize and deserialize correctly" in {

      val data = Json.obj("key" -> "value")

      val saveForLaterRequest = SaveForLaterRequest(
        vrn = vrn,
        data = data,
      )

      val json = Json.toJson(saveForLaterRequest)

      val expectedJson = Json.parse(
        s"""
        {
          "vrn": "123456789",
          "data": {
            "key": "value"
          }
        }
        """
      )

      json mustBe expectedJson

      val deserialized = json.as[SaveForLaterRequest]
      deserialized mustBe saveForLaterRequest
      deserialized.vrn mustBe vrn
      deserialized.data mustBe data
    }

    "handle empty data object" in {
      val emptyData = Json.obj()
      val request = SaveForLaterRequest(vrn, emptyData)

      request.vrn mustBe vrn
      request.data mustBe emptyData

      val json = Json.toJson(request)
      val expectedJson = Json.parse(s"""{"vrn":"123456789","data":{}}""")

      json mustBe expectedJson
      json.as[SaveForLaterRequest] mustBe request
    }

    "handle invalid data object" in {


      val json = Json.parse(
        s"""
              {
                "vrn": 123456789,
                "data": {
                  "key": "value"
                }
              }
              """
      )

      json.validate[SaveForLaterRequest] mustBe a[JsError]
    }

    "handle nested JSON structures" in {
      val nestedData = Json.obj(
        "level1" -> Json.obj(
          "level2" -> Json.obj("key" -> "value")
        )
      )

      val request = SaveForLaterRequest(vrn, nestedData)

      request.vrn mustBe vrn
      request.data mustBe nestedData

      val json = Json.toJson(request)
      json.as[SaveForLaterRequest] mustBe request
    }
  }
}

