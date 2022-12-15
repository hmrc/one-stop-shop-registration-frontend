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

package models.previousRegistrations

import models.Country

case class IossRegistrationNumberValidation(country: Country, vrnRegex: String, messageInput: String)

object IossRegistrationNumberValidation {

  lazy val euCountriesWithIOSSValidationRules: Seq[IossRegistrationNumberValidation] = Seq(
    IossRegistrationNumberValidation(Country("AT", "Austria"),austriaIossNumberRegex, "the 9 characters"),
    IossRegistrationNumberValidation(Country("BE", "Belgium"), belgiumIossNumberRegex, "the 10 numbers"),
    IossRegistrationNumberValidation(Country("BG", "Bulgaria"), bulgariaIossNumberRegex, "9 or 10 numbers"),
    IossRegistrationNumberValidation(Country("HR", "Croatia"), croatiaIossNumberRegex, "the 11 numbers"),
    IossRegistrationNumberValidation(Country("CY", "Republic of Cyprus"), cyprusIossNumberRegex, "the 9 characters"),
    IossRegistrationNumberValidation(Country("CZ", "Czech Republic"), czechRepublicIossNumberRegex, "8, 9 or 10 numbers"),
    IossRegistrationNumberValidation(Country("DK", "Denmark"), denmarkIossNumberRegex, "the 8 numbers"),
    IossRegistrationNumberValidation(Country("EE", "Estonia"), estoniaIossNumberRegex, "the 9 numbers"),
    IossRegistrationNumberValidation(Country("FI", "Finland"), finlandIossNumberRegex, "the 8 numbers"),
    IossRegistrationNumberValidation(Country("FR", "France"), franceIossNumberRegex, "the 11 characters"),
    IossRegistrationNumberValidation(Country("DE", "Germany"), germanyIossNumberRegex, "the 9 numbers"),
    IossRegistrationNumberValidation(Country("EL", "Greece"), greeceIossNumberRegex, "the 9 numbers"),
    IossRegistrationNumberValidation(Country("HU", "Hungary"), hungaryIossNumberRegex, "the 8 numbers"),
    IossRegistrationNumberValidation(Country("IE", "Ireland"), irelandIossNumberRegex, "8 or 9 characters"),
    IossRegistrationNumberValidation(Country("IT", "Italy"), italyIossNumberRegex, "the 11 numbers"),
    IossRegistrationNumberValidation(Country("LV", "Latvia"), latviaIossNumberRegex, "the 11 numbers"),
    IossRegistrationNumberValidation(Country("LT", "Lithuania"), lithuaniaIossNumberRegex, "9 or 12 numbers"),
    IossRegistrationNumberValidation(Country("LU", "Luxembourg"), luxembourgIossNumberRegex, "the 8 numbers"),
    IossRegistrationNumberValidation(Country("MT", "Malta"), maltaIossNumberRegex, "the 8 numbers"),
    IossRegistrationNumberValidation(Country("NL", "Netherlands"), netherlandsIossNumberRegex, "the 12 characters"),
    IossRegistrationNumberValidation(Country("PL", "Poland"), polandIossNumberRegex, "the 10 numbers"),
    IossRegistrationNumberValidation(Country("PT", "Portugal"), portugalIossNumberRegex, "the 9 numbers"),
    IossRegistrationNumberValidation(Country("RO", "Romania"), romaniaIossNumberRegex, "between 2 and 10 numbers"),
    IossRegistrationNumberValidation(Country("SK", "Slovakia"), slovakiaIossNumberRegex, "the 10 numbers"),
    IossRegistrationNumberValidation(Country("SI", "Slovenia"), sloveniaIossNumberRegex, "the 8 numbers"),
    IossRegistrationNumberValidation(Country("ES", "Spain"), spainIossNumberRegex, "the 9 characters"),
    IossRegistrationNumberValidation(Country("SE", "Sweden"), swedenIossNumberRegex, "the 12 numbers")
  )

  private val austriaIossNumberRegex       = """^IM040[0-9]{7}$"""
  private val belgiumIossNumberRegex       = """^IM056[0-9]{7}$"""
  private val bulgariaIossNumberRegex      = """^IM100[0-9]{7}$"""
  private val cyprusIossNumberRegex        = """^IM196[0-9]{7}$"""
  private val czechRepublicIossNumberRegex = """^IM203[0-9]{7}$"""
  private val germanyIossNumberRegex       = """^IM276[0-9]{7}$"""
  private val denmarkIossNumberRegex       = """^IM208[0-9]{7}$"""
  private val estoniaIossNumberRegex       = """^IM233[0-9]{7}$"""
  private val greeceIossNumberRegex        = """^IM300[0-9]{7}$"""
  private val spainIossNumberRegex         = """^IM724[0-9]{7}$"""
  private val finlandIossNumberRegex       = """^IM246[0-9]{7}$"""
  private val franceIossNumberRegex        = """^IM250[0-9]{7}$"""
  private val croatiaIossNumberRegex       = """^IM191[0-9]{7}$"""
  private val hungaryIossNumberRegex       = """^IM348[0-9]{7}$"""
  private val irelandIossNumberRegex       = """^IM372[0-9]{7}$"""
  private val italyIossNumberRegex         = """^IM380[0-9]{7}$"""
  private val lithuaniaIossNumberRegex     = """^IM440[0-9]{7}$"""
  private val luxembourgIossNumberRegex    = """^IM442[0-9]{7}$"""
  private val latviaIossNumberRegex        = """^IM428[0-9]{7}$"""
  private val maltaIossNumberRegex         = """^IM470[0-9]{7}$"""
  private val netherlandsIossNumberRegex   = """^IM528[0-9]{7}$"""
  private val polandIossNumberRegex        = """^IM616[0-9]{7}$"""
  private val portugalIossNumberRegex      = """^IM620[0-9]{7}$"""
  private val romaniaIossNumberRegex       = """^IM642[0-9]{7}$"""
  private val swedenIossNumberRegex        = """^IM752[0-9]{7}$"""
  private val sloveniaIossNumberRegex      = """^IM705[0-9]{7}$"""
  private val slovakiaIossNumberRegex      = """^IM703[0-9]{7}$"""
}