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

case class IntermediaryIdentificationNumberValidation(country: Country, vrnRegex: String, messageInput: String)

object IntermediaryIdentificationNumberValidation {

  lazy val euCountriesWithIntermediaryValidationRules: Seq[IntermediaryIdentificationNumberValidation] = Seq(
    IntermediaryIdentificationNumberValidation(Country("AT", "Austria"),austriaIntermediaryIdentificationRegex, "the 9 characters"),
    IntermediaryIdentificationNumberValidation(Country("BE", "Belgium"), belgiumIntermediaryIdentificationRegex, "the 10 numbers"),
    IntermediaryIdentificationNumberValidation(Country("BG", "Bulgaria"), bulgariaIntermediaryIdentificationRegex, "9 or 10 numbers"),
    IntermediaryIdentificationNumberValidation(Country("HR", "Croatia"), croatiaIntermediaryIdentificationRegex, "the 11 numbers"),
    IntermediaryIdentificationNumberValidation(Country("CY", "Republic of Cyprus"), cyprusIntermediaryIdentificationRegex, "the 9 characters"),
    IntermediaryIdentificationNumberValidation(Country("CZ", "Czech Republic"), czechRepublicIntermediaryIdentificationRegex, "8, 9 or 10 numbers"),
    IntermediaryIdentificationNumberValidation(Country("DK", "Denmark"), denmarkIntermediaryIdentificationRegex, "the 8 numbers"),
    IntermediaryIdentificationNumberValidation(Country("EE", "Estonia"), estoniaIntermediaryIdentificationRegex, "the 9 numbers"),
    IntermediaryIdentificationNumberValidation(Country("FI", "Finland"), finlandIntermediaryIdentificationRegex, "the 8 numbers"),
    IntermediaryIdentificationNumberValidation(Country("FR", "France"), franceIntermediaryIdentificationRegex, "the 11 characters"),
    IntermediaryIdentificationNumberValidation(Country("DE", "Germany"), germanyIntermediaryIdentificationRegex, "the 9 numbers"),
    IntermediaryIdentificationNumberValidation(Country("EL", "Greece"), greeceIntermediaryIdentificationRegex, "the 9 numbers"),
    IntermediaryIdentificationNumberValidation(Country("HU", "Hungary"), hungaryIntermediaryIdentificationRegex, "the 8 numbers"),
    IntermediaryIdentificationNumberValidation(Country("IE", "Ireland"), irelandIntermediaryIdentificationRegex, "8 or 9 characters"),
    IntermediaryIdentificationNumberValidation(Country("IT", "Italy"), italyIntermediaryIdentificationRegex, "the 11 numbers"),
    IntermediaryIdentificationNumberValidation(Country("LV", "Latvia"), latviaIntermediaryIdentificationRegex, "the 11 numbers"),
    IntermediaryIdentificationNumberValidation(Country("LT", "Lithuania"), lithuaniaIntermediaryIdentificationRegex, "9 or 12 numbers"),
    IntermediaryIdentificationNumberValidation(Country("LU", "Luxembourg"), luxembourgIntermediaryIdentificationRegex, "the 8 numbers"),
    IntermediaryIdentificationNumberValidation(Country("MT", "Malta"), maltaIntermediaryIdentificationRegex, "the 8 numbers"),
    IntermediaryIdentificationNumberValidation(Country("NL", "Netherlands"), netherlandsIntermediaryIdentificationRegex, "the 12 characters"),
    IntermediaryIdentificationNumberValidation(Country("PL", "Poland"), polandIntermediaryIdentificationRegex, "the 10 numbers"),
    IntermediaryIdentificationNumberValidation(Country("PT", "Portugal"), portugalIntermediaryIdentificationRegex, "the 9 numbers"),
    IntermediaryIdentificationNumberValidation(Country("RO", "Romania"), romaniaIntermediaryIdentificationRegex, "between 2 and 10 numbers"),
    IntermediaryIdentificationNumberValidation(Country("SK", "Slovakia"), slovakiaIntermediaryIdentificationRegex, "the 10 numbers"),
    IntermediaryIdentificationNumberValidation(Country("SI", "Slovenia"), sloveniaIntermediaryIdentificationRegex, "the 8 numbers"),
    IntermediaryIdentificationNumberValidation(Country("ES", "Spain"), spainIntermediaryIdentificationRegex, "the 9 characters"),
    IntermediaryIdentificationNumberValidation(Country("SE", "Sweden"), swedenIntermediaryIdentificationRegex, "the 12 numbers")
  )

  private val austriaIntermediaryIdentificationRegex       = """^IN040[0-9]{7}$"""
  private val belgiumIntermediaryIdentificationRegex       = """^IN056[0-9]{7}$"""
  private val bulgariaIntermediaryIdentificationRegex      = """^IN100[0-9]{7}$"""
  private val cyprusIntermediaryIdentificationRegex        = """^IN196[0-9]{7}$"""
  private val czechRepublicIntermediaryIdentificationRegex = """^IN203[0-9]{7}$"""
  private val germanyIntermediaryIdentificationRegex       = """^IN276[0-9]{7}$"""
  private val denmarkIntermediaryIdentificationRegex       = """^IN208[0-9]{7}$"""
  private val estoniaIntermediaryIdentificationRegex       = """^IN233[0-9]{7}$"""
  private val greeceIntermediaryIdentificationRegex        = """^IN300[0-9]{7}$"""
  private val spainIntermediaryIdentificationRegex         = """^IN724[0-9]{7}$"""
  private val finlandIntermediaryIdentificationRegex       = """^IN246[0-9]{7}$"""
  private val franceIntermediaryIdentificationRegex        = """^IN250[0-9]{7}$"""
  private val croatiaIntermediaryIdentificationRegex       = """^IN191[0-9]{7}$"""
  private val hungaryIntermediaryIdentificationRegex       = """^IN348[0-9]{7}$"""
  private val irelandIntermediaryIdentificationRegex       = """^IN372[0-9]{7}$"""
  private val italyIntermediaryIdentificationRegex         = """^IN380[0-9]{7}$"""
  private val lithuaniaIntermediaryIdentificationRegex     = """^IN440[0-9]{7}$"""
  private val luxembourgIntermediaryIdentificationRegex    = """^IN442[0-9]{7}$"""
  private val latviaIntermediaryIdentificationRegex        = """^IN428[0-9]{7}$"""
  private val maltaIntermediaryIdentificationRegex         = """^IN470[0-9]{7}$"""
  private val netherlandsIntermediaryIdentificationRegex   = """^IN528[0-9]{7}$"""
  private val polandIntermediaryIdentificationRegex        = """^IN616[0-9]{7}$"""
  private val portugalIntermediaryIdentificationRegex      = """^IN620[0-9]{7}$"""
  private val romaniaIntermediaryIdentificationRegex       = """^IN642[0-9]{7}$"""
  private val swedenIntermediaryIdentificationRegex        = """^IN752[0-9]{7}$"""
  private val sloveniaIntermediaryIdentificationRegex      = """^IN705[0-9]{7}$"""
  private val slovakiaIntermediaryIdentificationRegex      = """^IN703[0-9]{7}$"""
}