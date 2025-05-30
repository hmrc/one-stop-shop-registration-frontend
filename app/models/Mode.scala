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

import play.api.mvc.JavascriptLiteral

sealed trait Mode {
  def isInAmendOrRejoin: Boolean =
    isInAmend || isInRejoin

  def isInAmend: Boolean = {
    this == AmendMode || this == AmendLoopMode
  }

  def isInRejoin: Boolean = {
    this == RejoinMode || this == RejoinLoopMode
  }
}

case object CheckMode extends Mode

case object NormalMode extends Mode

case object CheckLoopMode extends Mode
case object AmendMode extends Mode
case object AmendLoopMode extends Mode
case object RejoinMode extends Mode
case object RejoinLoopMode extends Mode

object Mode {

  implicit val jsLiteral: JavascriptLiteral[Mode] = new JavascriptLiteral[Mode] {
    override def to(value: Mode): String = value match {
      case NormalMode     => "NormalMode"
      case CheckMode      => "CheckMode"
      case CheckLoopMode  => "CheckLoopMode"
      case AmendMode      => "AmendMode"
      case AmendLoopMode  => "AmendLoopMode"
      case RejoinMode     => "RejoinMode"
      case RejoinLoopMode => "RejoinLoopMode"
    }
  }
}
