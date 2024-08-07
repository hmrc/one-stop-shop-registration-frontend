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

package pages

import models._
import play.api.mvc.Call

import scala.language.implicitConversions

trait Page {

  def navigate(mode: Mode, answers: UserAnswers): Call = mode match {
    case NormalMode => navigateInNormalMode(answers)
    case CheckMode => navigateInCheckMode(answers)
    case CheckLoopMode => navigateInCheckLoopMode(answers)
    case AmendMode => navigateInAmendMode(answers)
    case AmendLoopMode => navigateInAmendLoopMode(answers)
    case RejoinMode => navigateInRejoinMode(answers)
    case RejoinLoopMode => navigateInRejoinLoopMode(answers)
  }

  protected def navigateInNormalMode(answers: UserAnswers): Call =
    throw new NotImplementedError("navigateInNormalMode is not implemented on this page")

  protected def navigateInCheckMode(answers: UserAnswers): Call =
    throw new NotImplementedError("navigateInCheckMode is not implemented on this page")

  protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    throw new NotImplementedError("navigateInCheckLoopMode is not implemented on this page")

  protected def navigateInAmendMode(answers: UserAnswers): Call =
    throw new NotImplementedError("navigateInAmendMode is not implemented on this page")

  protected def navigateInAmendLoopMode(answers: UserAnswers): Call =
    throw new NotImplementedError("navigateInAmendLoopMode is not implemented on this page")

  protected def navigateInRejoinMode(answers: UserAnswers): Call =
    throw new NotImplementedError("navigateInRejoinMode is not implemented on this page")

  protected def navigateInRejoinLoopMode(answers: UserAnswers): Call =
    throw new NotImplementedError("navigateInRejoinLoopMode is not implemented on this page")
}

object Page {

  implicit def toString(page: Page): String =
    page.toString
}
