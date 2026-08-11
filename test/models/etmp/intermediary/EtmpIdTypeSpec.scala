/*
 * Copyright 2026 HM Revenue & Customs
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

package models.etmp.intermediary

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class EtmpIdTypeSpec extends SpecBase with ScalaCheckPropertyChecks {

  "EtmpIdType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(EtmpIdType.values)

      forAll(gen) {
        etmpIdType =>

          JsString(etmpIdType.toString).validate[EtmpIdType].asOpt.value mustBe etmpIdType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String].suchThat(!EtmpIdType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValues =>

          JsString(invalidValues).validate[EtmpIdType] mustBe JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(EtmpIdType.values)

      forAll(gen) {
        etmpIdType =>

          Json.toJson(etmpIdType) mustBe JsString(etmpIdType.toString)
      }
    }
  }
}

