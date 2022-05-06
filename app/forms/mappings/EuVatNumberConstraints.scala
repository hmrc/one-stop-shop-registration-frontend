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

package forms.mappings

import play.api.data.validation.{Constraint, Invalid, Valid}

trait EuVatNumberConstraints {

  val austriaVatNumberRegex = """^U[0-9]{8}$"""
  val belgiumVatNumberRegex = """^(0|1)[0-9]{9}$"""
  val bulgariaVatNumberRegex = """^[0-9]{9,10}$"""
  val cyprusVatNumberRegex = """^[0-9]{8}[A-Z]$"""
  val czechRepublicVatNumberRegex = """^[0-9]{8,10}$"""
  val germanyVatNumberRegex = """^[0-9]{9}$"""
  val denmarkVatNumberRegex = """^[0-9]{8}$"""
  val estoniaVatNumberRegex = """^[0-9]{9}$"""
  val greeceVatNumberRegex = """^[0-9]{9}$"""
  val spainVatNumberRegex = """^[A-Z][0-9]{8}$|^[0-9]{8}[A-Z]$|^[A-Z][0-9]{7}[A-Z]$"""
  val finlandVatNumberRegex = """^[0-9]{8}$"""
  val franceVatNumberRegex = """^[A-Z0-9]{2}[0-9]{9}$"""
  val croatiaVatNumberRegex = """^[0-9]{11}$"""
  val hungaryVatNumberRegex = """^[0-9]{8}$"""
  val irelandVatNumberRegex = """^[0-9][A-Z0-9\+\*][0-9]{5}[A-Z]$|^[0-9]{7}WI$"""
  val italyVatNumberRegex = """^[0-9]{11}$"""
  val lithuaniaVatNumberRegex = """^[0-9]{9}$|^[0-9]{12}$"""
  val luxembourgVatNumberRegex = """^[0-9]{8}$"""
  val latviaVatNumberRegex = """^[0-9]{11}$"""
  val maltaVatNumberRegex = """^[0-9]{8}$"""
  val netherlandsVatNumberRegex = """^[A-Z0-9\+\*]{12}$"""
  val polandVatNumberRegex = """^[0-9]{10}$"""
  val portugalVatNumberRegex = """^[0-9]{9}$"""
  val romaniaVatNumberRegex = """^[0-9]{2,10}$"""
  val swedenVatNumberRegex = """^[0-9]{12}$"""
  val sloveniaVatNumberRegex = """^[0-9]{8}$"""
  val slovakiaVatNumberRegex = """^[0-9]{10}$"""


  def validateEuVatNumber(countryCode: String, errorKey: String): Constraint[String] = {
    Constraint {
      input =>

        val regex = getCountryVatRegex(countryCode)

        if(input.matches(regex)) {
          Valid
        } else {
          Invalid(errorKey)
        }
    }
  }

  private def getCountryVatRegex(countryCode: String): String = countryCode match {
    case "AT" => austriaVatNumberRegex
    case "BE" => belgiumVatNumberRegex
    case "BG" => bulgariaVatNumberRegex
    case "HR" => croatiaVatNumberRegex
    case "CY" => cyprusVatNumberRegex
    case "CZ" => czechRepublicVatNumberRegex
    case "DK" => denmarkVatNumberRegex
    case "EE" => estoniaVatNumberRegex
    case "FI" => finlandVatNumberRegex
    case "FR" => franceVatNumberRegex
    case "DE" => germanyVatNumberRegex
    case "EL" => greeceVatNumberRegex
    case "HU" => hungaryVatNumberRegex
    case "IE" => irelandVatNumberRegex
    case "IT" => italyVatNumberRegex
    case "LV" => latviaVatNumberRegex
    case "LT" => lithuaniaVatNumberRegex
    case "LU" => luxembourgVatNumberRegex
    case "MT" => maltaVatNumberRegex
    case "NL" => netherlandsVatNumberRegex
    case "PL" => polandVatNumberRegex
    case "PT" => portugalVatNumberRegex
    case "RO" => romaniaVatNumberRegex
    case "SK" => slovakiaVatNumberRegex
    case "SI" => sloveniaVatNumberRegex
    case "ES" => spainVatNumberRegex
    case "SE" => swedenVatNumberRegex
    case _ => throw new Exception("invalid country code")
  }


}
