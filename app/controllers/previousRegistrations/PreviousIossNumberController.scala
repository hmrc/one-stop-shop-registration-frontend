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

package controllers.previousRegistrations

import controllers.actions._
import forms.previousRegistrations.PreviousIossNumberFormProvider
import logging.Logging
import models.{Country, Index, Mode}
import models.requests.AuthenticatedDataRequest
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousIossNumberPage, PreviousIossSchemePage}
import views.html.previousRegistrations.PreviousIossNumberView
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousIossNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: PreviousIossNumberFormProvider,
                                        view: PreviousIossNumberView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(countryIndex) { country =>
        getHasIntermediary(countryIndex, schemeIndex) { hasIntermediary =>

          val preparedForm = request.userAnswers.get(PreviousIossNumberPage(countryIndex, schemeIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, countryIndex, schemeIndex, country, hasIntermediary)))
        }
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(countryIndex) { country =>
        getHasIntermediary(countryIndex, schemeIndex) { hasIntermediary =>

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, countryIndex, schemeIndex, country, hasIntermediary))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviousIossNumberPage(countryIndex, schemeIndex), value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(PreviousIossNumberPage(countryIndex, schemeIndex).navigate(mode, updatedAnswers))
          )
        }
      }
  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(PreviousEuCountryPage(index)).map {
      country =>
        block(country)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  private def getHasIntermediary(countryIndex: Index, schemeIndex: Index)
                        (block: Boolean => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(PreviousIossSchemePage(countryIndex, schemeIndex)).map {
      hasIntermediary =>
        block(hasIntermediary)
    }.getOrElse{
      logger.error("Failed to get intermediary")
      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
}
