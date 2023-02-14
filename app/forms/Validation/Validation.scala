/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.Validation

object Validation {

  val euVatNumberPattern = """^[A-Za-z\d\*\+]{1,12}$"""
  val postcodePattern = """^[A-Za-z0-9 ]{0,100}$"""
  val commonTextPattern = """^[A-Za-z0-9À-ÿ \!\)\(.,_/’'"&-]+$"""
  val bankAccountNamePattern = """^[A-Za-z0-9/\-?:().,'+ ]*$"""
  val postCodePattern = """^[ ]*[A-Za-z][ ]*[A-Za-z]{0,1}[ ]*[0-9][ ]*[0-9A-Za-z]{0,1}[ ]*[0-9][ ]*[A-Za-z][ ]*[A-Za-z][ ]*$"""
  val emailPattern = """^(.+)\b@\b(.+)\b\.\b(.+)$"""
  val telephonePattern = """^\+[0-9 ]{1,18}$|^[0-9 ]{1,19}$|^(?=.{2,22}$)\+[0-9 ]*\(0\)[0-9 ]*$|^(?=.{1,22}$)[0-9 ]*\(0\)[0-9 ]*$"""
  val websitePattern = """^(((HTTP|http)(S|s)?\:\/\/((WWW|www)\.)?)|(( |WWW|www)\.))?[a-zA-Z0-9\[_~\:\/?\-#\]@!&'()*+, |;=% ]+\.[a-zA-Z]{2,5}(\.[a-zA-Z]{2,5})?(\:[0-9] |{1,5})?(\/[a-zA-Z0-9_-]+(\/)?)*$"""
  val alphaNumericWithSpace = """^[a-zA-Z0-9 ]+$"""
}
