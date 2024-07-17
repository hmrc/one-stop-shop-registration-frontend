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

import models.etmp.SchemeType
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait PreviousScheme

object PreviousScheme extends Enumerable.Implicits {

  def fromEmtpSchemeType(etmpSchemeType: SchemeType): PreviousScheme = {
    etmpSchemeType match {
      case SchemeType.OSSUnion => OSSU
      case SchemeType.OSSNonUnion => OSSNU
      case SchemeType.IOSSWithoutIntermediary => IOSSWOI
      case SchemeType.IOSSWithIntermediary => IOSSWI
    }
  }

  def toEmtpSchemaType(previousScheme: PreviousScheme): SchemeType = {
    previousScheme match {
      case OSSU => SchemeType.OSSUnion
      case OSSNU => SchemeType.OSSNonUnion
      case IOSSWOI => SchemeType.IOSSWithoutIntermediary
      case IOSSWI => SchemeType.IOSSWithIntermediary
    }
  }

  case object OSSU extends WithName("ossu") with PreviousScheme

  case object OSSNU extends WithName("ossnu") with PreviousScheme

  case object IOSSWOI extends WithName("iosswoi") with PreviousScheme

  case object IOSSWI extends WithName("iosswi") with PreviousScheme

  val values: Seq[PreviousScheme] = Seq(
    OSSU, OSSNU, IOSSWOI, IOSSWI
  )

  val iossValues: Seq[PreviousScheme] = Seq(
    IOSSWOI, IOSSWI
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"previousScheme.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  def iossOptions(implicit messages: Messages): Seq[RadioItem] = iossValues.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"previousScheme.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[PreviousScheme] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
