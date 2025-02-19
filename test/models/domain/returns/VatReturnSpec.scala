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

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDate, ZoneId}


class VatReturnSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with Generators {

  private val instant = Instant.now
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  "VAT Return" - {

    "must serialise and deserialise from and to an VatReturn" in {

      val json = Json.obj(
        "reference" -> "XI/XI123456789/Q3.2021",
        "salesFromNi" -> Json.arr(
          Json.obj(
            "countryOfConsumption" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"),
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
        "lastUpdated" -> Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS),
        "paymentReference" -> "NI123456789Q321",
        "vrn" -> "123456789",
        "period" -> Json.obj(
          "year" -> 2021,
          "quarter" -> "Q3"
        ),
        "endDate" -> "2021-04-21",
        "salesFromEu" -> Json.arr(
          Json.obj(
            "countryOfSale" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "sales" -> Json.arr(
              Json.obj(
                "countryOfConsumption" -> Json.obj(
                  "code" -> "FI",
                  "name" -> "Finland"
                ),
                "amounts" -> Json.arr(
                  Json.obj(
                    "vatRate" -> Json.obj(
                      "rate" -> 56.02,
                      "rateType" -> "STANDARD"
                    ),
                    "netValueOfSales" -> 543742.51,
                    "vatOnSales" -> Json.obj(
                      "choice" -> "standard",
                      "amount" -> 801143.05
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
        ),
        "submissionReceived" -> "2023-07-01T12:00:00Z",
        "startDate" -> "2021-03-12"
      )

      val expectedResult = VatReturn(
        vrn = vrn,
        period = period,
        reference = ReturnReference(vrn, period),
        paymentReference = PaymentReference(vrn, period),
        startDate = Some(LocalDate.of(2021, 3, 12)),
        endDate = Some(LocalDate.of(2021, 4, 21)),
        salesFromNi = List(SalesToCountry(
          Country("DE", "Germany"),
          List(SalesDetails(
            VatRate(20.00, Standard),
            2000.00,
            VatOnSales(VatOnSalesChoice.Standard, 400.00)
          ))
        )),
        salesFromEu = List(SalesFromEuCountry(
          Country("DE", "Germany"),
          Some(EuTaxIdentifier(Vat, Some("-1"))),
          List(SalesToCountry(
            Country("FI", "Finland"),
            List(SalesDetails(
              VatRate(56.02, VatRateType.Standard),
              543742.51,
              VatOnSales(VatOnSalesChoice.Standard, 801143.05)
            ))
          ))
        )),
        submissionReceived = Instant.parse("2023-07-01T12:00:00Z"),
        lastUpdated = Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS)
      )


      Json.toJson(expectedResult) mustBe json
      json.validate[VatReturn] mustBe JsSuccess(expectedResult)
    }

    "must handle optional missing fields during deserialization" in {

      val json = Json.obj(
        "reference" -> "XI/XI123456789/Q3.2021",
        "salesFromNi" -> Json.arr(
          Json.obj(
            "countryOfConsumption" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"),
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
        "lastUpdated" -> Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS),
        "paymentReference" -> "NI123456789Q321",
        "vrn" -> "123456789",
        "period" -> Json.obj(
          "year" -> 2021,
          "quarter" -> "Q3"
        ),
        "salesFromEu" -> Json.arr(
          Json.obj(
            "countryOfSale" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "sales" -> Json.arr(
              Json.obj(
                "countryOfConsumption" -> Json.obj(
                  "code" -> "FI",
                  "name" -> "Finland"
                ),
                "amounts" -> Json.arr(
                  Json.obj(
                    "vatRate" -> Json.obj(
                      "rate" -> 56.02,
                      "rateType" -> "STANDARD"
                    ),
                    "netValueOfSales" -> 543742.51,
                    "vatOnSales" -> Json.obj(
                      "choice" -> "standard",
                      "amount" -> 801143.05
                    )
                  )
                )
              )
            ),
          )
        ),
        "submissionReceived" -> "2023-07-01T12:00:00Z"
      )

      val expectedResult = VatReturn(
        vrn = vrn,
        period = period,
        reference = ReturnReference(vrn, period),
        paymentReference = PaymentReference(vrn, period),
        startDate = None,
        endDate = None,
        salesFromNi = List(SalesToCountry(
          Country("DE", "Germany"),
          List(SalesDetails(
            VatRate(20.00, Standard),
            2000.00,
            VatOnSales(VatOnSalesChoice.Standard, 400.00)
          ))
        )),
        salesFromEu = List(SalesFromEuCountry(
          Country("DE", "Germany"),
          None,
          List(SalesToCountry(
            Country("FI", "Finland"),
            List(SalesDetails(
              VatRate(56.02, VatRateType.Standard),
              543742.51,
              VatOnSales(VatOnSalesChoice.Standard, 801143.05)
            ))
          ))
        )),
        submissionReceived = Instant.parse("2023-07-01T12:00:00Z"),
        lastUpdated = Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS)
      )


      Json.toJson(expectedResult) mustBe json
      json.validate[VatReturn] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[VatReturn] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "reference" -> 12345, //invalid
        "salesFromNi" -> Json.arr(
          Json.obj(
            "countryOfConsumption" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"),
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
        "lastUpdated" -> Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS),
        "paymentReference" -> "NI123456789Q321",
        "vrn" -> "123456789",
        "period" -> Json.obj(
          "year" -> 2021,
          "quarter" -> "Q3"
        ),
        "salesFromEu" -> Json.arr(
          Json.obj(
            "countryOfSale" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "sales" -> Json.arr(
              Json.obj(
                "countryOfConsumption" -> Json.obj(
                  "code" -> "FI",
                  "name" -> "Finland"
                ),
                "amounts" -> Json.arr(
                  Json.obj(
                    "vatRate" -> Json.obj(
                      "rate" -> 56.02,
                      "rateType" -> "STANDARD"
                    ),
                    "netValueOfSales" -> 543742.51,
                    "vatOnSales" -> Json.obj(
                      "choice" -> "standard",
                      "amount" -> 801143.05
                    )
                  )
                )
              )
            ),
          )
        ),
        "submissionReceived" -> "2023-07-01T12:00:00Z"
      )

      json.validate[VatReturn] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "reference" -> JsNull, //null
        "salesFromNi" -> Json.arr(
          Json.obj(
            "countryOfConsumption" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"),
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
        "lastUpdated" -> Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS),
        "paymentReference" -> "NI123456789Q321",
        "vrn" -> "123456789",
        "period" -> Json.obj(
          "year" -> 2021,
          "quarter" -> "Q3"
        ),
        "salesFromEu" -> Json.arr(
          Json.obj(
            "countryOfSale" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "sales" -> Json.arr(
              Json.obj(
                "countryOfConsumption" -> Json.obj(
                  "code" -> "FI",
                  "name" -> "Finland"
                ),
                "amounts" -> Json.arr(
                  Json.obj(
                    "vatRate" -> Json.obj(
                      "rate" -> 56.02,
                      "rateType" -> "STANDARD"
                    ),
                    "netValueOfSales" -> 543742.51,
                    "vatOnSales" -> Json.obj(
                      "choice" -> "standard",
                      "amount" -> 801143.05
                    )
                  )
                )
              )
            ),
          )
        ),
        "submissionReceived" -> "2023-07-01T12:00:00Z"
      )

      json.validate[VatReturn] mustBe a[JsError]
    }

  }
}
