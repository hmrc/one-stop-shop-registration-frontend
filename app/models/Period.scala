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

package models

import models.Quarter.{Q1, Q2, Q3, Q4}
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.{PathBindable, QueryStringBindable}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.util.matching.Regex

case class Period(year: Int, quarter: Quarter) {

  val firstDay: LocalDate = LocalDate.of(year, quarter.startMonth, 1)
  val lastDay: LocalDate = firstDay.plusMonths(3).minusDays(1)
  val paymentDeadline: LocalDate = firstDay.plusMonths(4).minusDays(1)

  private val firstDayFormatter = DateTimeFormatter.ofPattern("d MMMM")
  private val lastDayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val firstMonthFormatter = DateTimeFormatter.ofPattern("MMMM")
  private val lastMonthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

  def displayText(implicit messages: Messages): String =
    s"${firstDay.format(firstDayFormatter)} ${messages("site.to")} ${lastDay.format(lastDayFormatter)}"

  def displayShortText(implicit messages: Messages): String =
    s"${firstDay.format(firstMonthFormatter)} ${messages("site.to")} ${lastDay.format(lastMonthYearFormatter)}"

  override def toString: String = s"$year-${quarter.toString}"

  def getPreviousPeriod: Period = {
    quarter match {
      case Q4 =>
        Period(year, Q3)
      case Q3 =>
        Period(year, Q2)
      case Q2 =>
        Period(year, Q1)
      case Q1 =>
        Period(year - 1, Q4)
    }
  }
}

object Period {

  private val pattern: Regex = """(\d{4})-(Q[1-4])""".r.anchored

  def apply(yearString: String, quarterString: String): Try[Period] =
    for {
      year <- Try(yearString.toInt)
      quarter <- Quarter.fromString(quarterString)
    } yield Period(year, quarter)

  def fromString(string: String): Option[Period] =
    string match {
      case pattern(yearString, quarterString) =>
        Period(yearString, quarterString).toOption
      case _ =>
        None
    }

  implicit val format: OFormat[Period] = Json.format[Period]

  implicit val pathBindable: PathBindable[Period] = new PathBindable[Period] {

    override def bind(key: String, value: String): Either[String, Period] =
      fromString(value) match {
        case Some(period) => Right(period)
        case None => Left("Invalid period")
      }

    override def unbind(key: String, value: Period): String =
      value.toString
  }

  implicit val queryBindable: QueryStringBindable[Period] = new QueryStringBindable[Period] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Period]] = {
      params.get(key).flatMap(_.headOption).map {
        periodString =>
          fromString(periodString) match {
            case Some(period) => Right(period)
            case _ => Left("Invalid period")
          }
      }
    }

    override def unbind(key: String, value: Period): String = {
      s"$key=${value.toString}"
    }
  }

}

