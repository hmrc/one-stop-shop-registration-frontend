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

package forms.Validation

object Validation {

  val euVatNumberPattern = """^[A-Za-z\d\*\+]{1,12}$"""
  val postcodePattern = """^[A-Za-z0-9 ]{0,100}$"""
  val commonTextPattern = """^(?!^[’'"])(?:[A-Za-z0-9À-ÿ \!\)\(.,_/’'"&-]|[’'"](?=[A-Za-z0-9À-ÿ \!\)\(.,_/’'"&-]))*[A-Za-z0-9À-ÿ \!\)\(.,_/’'"&-](?<![’'"]$)$"""
  val bankAccountNamePattern = """^[A-Za-z0-9/\-?:()\.,'+]+( [A-Za-z0-9/\-?:()\.,'+]+)*$"""
  val postCodePattern = """^[ ]*[A-Za-z][ ]*[A-Za-z]{0,1}[ ]*[0-9][ ]*[0-9A-Za-z]{0,1}[ ]*[0-9][ ]*[A-Za-z][ ]*[A-Za-z][ ]*$"""
  val emailPattern = """^(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])$"""
  val telephonePattern = """^\+[0-9 ]{1,18}$|^[0-9 ]{1,19}$"""
  val websitePattern = """^(https?://)((([a-z\d]([a-z\d-]*[a-z\d])*)\.)+[a-z]{2,})(\:\d+)?(\/[-a-z\d%_.~+]*)*(\?[;&a-z\d%_.~+=-]*)?(\#[-a-z\d_]*)?"""
  val alphaNumericWithSpace = """^[a-zA-Z0-9 ]+$"""
}
