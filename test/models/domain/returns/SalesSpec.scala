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

package models.domain.returns

import base.SpecBase
import generators.Generators
import models.Country
import models.domain.EuTaxIdentifier
import models.domain.EuTaxIdentifierType.Vat
import models.domain.returns.VatRateType.Standard
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class SalesSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with Generators {



  "Sales" - {

    ".SalesFromEuCountry" - {

      "must serialise and deserialise from and to an SalesFromEuCountry" in {

        val json = Json.obj(
          "countryOfSale" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "sales" -> Json.arr(
            Json.obj(
              "countryOfConsumption" -> Json.obj(
                "code" -> "DE",
                "name" -> "Germany"
              ),
              "amounts" -> Json.arr(
                Json.obj(
                  "vatRate" -> Json.obj(
                    "rate" -> 20,
                    "rateType" -> "STANDARD"
                  ),
                  "netValueOfSales" -> 2000,
                  "vatOnSales" -> Json.obj(
                    "choice" -> "standard",
                    "amount" -> 400
                  )
                )
              )
            )
          ),
          "taxIdentifier" -> Json.obj(
            "identifierType" -> "vat",
            "value" -> "-1"
          )
        )

        val expectedResult = SalesFromEuCountry(
          countryOfSale = Country("DE", "Germany"),
          taxIdentifier = Some(EuTaxIdentifier(Vat, Some("-1"))),
          sales = List(SalesToCountry(
            Country("DE", "Germany"),
            List(SalesDetails(
              VatRate(20.00, Standard),
              2000.00,
              VatOnSales(VatOnSalesChoice.Standard, 400.00)
            )))
          )
        )


        Json.toJson(expectedResult) mustBe json
        json.validate[SalesFromEuCountry] mustBe JsSuccess(expectedResult)
      }

      "must handle optional missing fields during deserialization" in {

        val json = Json.obj(
          "countryOfSale" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "sales" -> Json.arr(
            Json.obj(
              "countryOfConsumption" -> Json.obj(
                "code" -> "DE",
                "name" -> "Germany"
              ),
              "amounts" -> Json.arr(
                Json.obj(
                  "vatRate" -> Json.obj(
                    "rate" -> 20,
                    "rateType" -> "STANDARD"
                  ),
                  "netValueOfSales" -> 2000,
                  "vatOnSales" -> Json.obj(
                    "choice" -> "standard",
                    "amount" -> 400
                  )
                )
              )
            )
          )
        )

        val expectedResult = SalesFromEuCountry(
          countryOfSale = Country("DE", "Germany"),
          taxIdentifier = None,
          sales = List(SalesToCountry(
            Country("DE", "Germany"),
            List(SalesDetails(
              VatRate(20.00, Standard),
              2000.00,
              VatOnSales(VatOnSalesChoice.Standard, 400.00)
            )))
          )
        )


        Json.toJson(expectedResult) mustBe json
        json.validate[SalesFromEuCountry] mustBe JsSuccess(expectedResult)
      }

      "must handle missing fields during deserialization" in {

        val json = Json.obj()

        json.validate[SalesFromEuCountry] mustBe a[JsError]
      }

      "must handle invalid data during deserialization" in {

        val json = Json.obj(
          "countryOfSale" -> Json.obj(
            "code" -> 12345, // invalid
            "name" -> "Germany"
          ),
          "sales" -> Json.arr(
            Json.obj(
              "countryOfConsumption" -> Json.obj(
                "code" -> "DE",
                "name" -> "Germany"
              ),
              "amounts" -> Json.arr(
                Json.obj(
                  "vatRate" -> Json.obj(
                    "rate" -> 20,
                    "rateType" -> "STANDARD"
                  ),
                  "netValueOfSales" -> 2000,
                  "vatOnSales" -> Json.obj(
                    "choice" -> "standard",
                    "amount" -> 400
                  )
                )
              )
            )
          ),
          "taxIdentifier" -> Json.obj(
            "identifierType" -> "vat",
            "value" -> "-1"
          )
        )

        json.validate[SalesFromEuCountry] mustBe a[JsError]
      }

      "must handle null data during deserialization" in {

        val json = Json.obj(
          "countryOfSale" -> Json.obj(
            "code" -> "DE",
            "name" -> JsNull // null
          ),
          "sales" -> Json.arr(
            Json.obj(
              "countryOfConsumption" -> Json.obj(
                "code" -> "DE",
                "name" -> "Germany"
              ),
              "amounts" -> Json.arr(
                Json.obj(
                  "vatRate" -> Json.obj(
                    "rate" -> 20,
                    "rateType" -> "STANDARD"
                  ),
                  "netValueOfSales" -> 2000,
                  "vatOnSales" -> Json.obj(
                    "choice" -> "standard",
                    "amount" -> 400
                  )
                )
              )
            )
          ),
          "taxIdentifier" -> Json.obj(
            "identifierType" -> "vat",
            "value" -> "-1"
          )
        )

        json.validate[SalesFromEuCountry] mustBe a[JsError]
      }
    }

    ".SalesToCountry" - {

      "must serialise and deserialise from and to an SalesToCountry" in {

        val json = Json.obj(
          "countryOfConsumption" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "amounts" -> Json.arr(
            Json.obj(
              "vatRate" -> Json.obj(
                "rate" -> 20,
                "rateType" -> "STANDARD"
              ),
              "netValueOfSales" -> 2000,
              "vatOnSales" -> Json.obj(
                "choice" -> "standard",
                "amount" -> 400
              )
            )
          )
        )

        val expectedResult = SalesToCountry(
          countryOfConsumption = Country("DE", "Germany"),
          amounts = List(SalesDetails(
            VatRate(20.00, Standard),
            2000.00,
            VatOnSales(VatOnSalesChoice.Standard, 400.00)
          ))
        )


        Json.toJson(expectedResult) mustBe json
        json.validate[SalesToCountry] mustBe JsSuccess(expectedResult)
      }

      "must handle missing fields during deserialization" in {

        val json = Json.obj()

        json.validate[SalesToCountry] mustBe a[JsError]
      }

      "must handle invalid data during deserialization" in {

        val json = Json.obj(
          "countryOfConsumption" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "amounts" -> Json.arr(
            Json.obj(
              "vatRate" -> Json.obj(
                "rate" -> 20,
                "rateType" -> 12345 // invalid
              ),
              "netValueOfSales" -> 2000,
              "vatOnSales" -> Json.obj(
                "choice" -> "standard",
                "amount" -> 400
              )
            )
          )
        )

        json.validate[SalesToCountry] mustBe a[JsError]
      }

      "must handle null data during deserialization" in {

        val json = Json.obj(
          "countryOfConsumption" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "amounts" -> Json.arr(
            Json.obj(
              "vatRate" -> Json.obj(
                "rate" -> 20,
                "rateType" -> "STANDARD"
              ),
              "netValueOfSales" -> JsNull, // null
              "vatOnSales" -> Json.obj(
                "choice" -> "standard",
                "amount" -> 400
              )
            )
          )
        )

        json.validate[SalesToCountry] mustBe a[JsError]
      }
    }

    ".SalesDetails" - {

      "must serialise and deserialise from and to an SalesDetails" in {

        val json = Json.obj(
          "vatRate" -> Json.obj(
            "rate" -> 20,
            "rateType" -> "STANDARD"
          ),
          "netValueOfSales" -> 2000,
          "vatOnSales" -> Json.obj(
            "choice" -> "standard",
            "amount" -> 400
          )
        )

        val expectedResult = SalesDetails(
          vatRate = VatRate(20.00, Standard),
          netValueOfSales = 2000.00,
          vatOnSales = VatOnSales(VatOnSalesChoice.Standard, 400.00)
        )


        Json.toJson(expectedResult) mustBe json
        json.validate[SalesDetails] mustBe JsSuccess(expectedResult)
      }

      "must handle missing fields during deserialization" in {

        val json = Json.obj()

        json.validate[SalesDetails] mustBe a[JsError]
      }

      "must handle invalid data during deserialization" in {

        val json = Json.obj(
          "vatRate" -> Json.obj(
            "rate" -> 20,
            "rateType" -> 12345 // invalid
          ),
          "netValueOfSales" -> 2000,
          "vatOnSales" -> Json.obj(
            "choice" -> "standard",
            "amount" -> 400
          )
        )

        json.validate[SalesDetails] mustBe a[JsError]
      }

      "must handle null data during deserialization" in {

        val json = Json.obj(
          "vatRate" -> Json.obj(
            "rate" -> 20,
            "rateType" -> "STANDARD"
          ),
          "netValueOfSales" -> 2000,
          "vatOnSales" -> Json.obj(
            "choice" -> "standard",
            "amount" -> JsNull // null
          )
        )

        json.validate[SalesDetails] mustBe a[JsError]
      }
    }

  }
}
