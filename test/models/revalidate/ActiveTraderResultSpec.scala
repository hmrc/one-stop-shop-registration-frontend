package models.revalidate

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.LocalDate

class ActiveTraderResultSpec extends SpecBase {

  private val now: LocalDate = LocalDate.now(stubClockAtArbitraryDate)

  "ActiveTraderResult" - {

    "must deserialise/serialise from and to ActiveTraderResult" - {

      "when all optional values are present" in {

        val activeTraderResult: ActiveTraderResult = ActiveTraderResult(
          isReversal = true,
          exclusionEffectiveDate = Some(now)
        )

        val expectedJson = Json.obj(
          "isReversal" -> true,
          "exclusionEffectiveDate" -> s"${now.toString}"
        )

        Json.toJson(activeTraderResult) `mustBe` expectedJson
        expectedJson.validate[ActiveTraderResult] `mustBe` JsSuccess(activeTraderResult)
      }

      "when all optional values are missing" in {

        val activeTraderResult: ActiveTraderResult = ActiveTraderResult(
          isReversal = true,
          exclusionEffectiveDate = None
        )

        val expectedJson = Json.obj(
          "isReversal" -> true
        )

        Json.toJson(activeTraderResult) `mustBe` expectedJson
        expectedJson.validate[ActiveTraderResult] `mustBe` JsSuccess(activeTraderResult)
      }
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[ActiveTraderResult] `mustBe` a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "isReversal" -> true,
        "exclusionEffectiveDate" -> "ABC"
      )

      json.validate[ActiveTraderResult] `mustBe` a[JsError]
    }
  }
}
