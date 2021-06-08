/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import controllers.actions._
import forms.CurrentlyRegisteredInCountryFormProvider
import models.requests.DataRequest
import models.{Country, Mode, UserAnswers}
import pages.{CurrentCountryOfRegistrationPage, CurrentlyRegisteredInCountryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.AllEuDetailsQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CurrentlyRegisteredInCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentlyRegisteredInCountryController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: CurrentlyRegisteredInCountryFormProvider,
                                         view: CurrentlyRegisteredInCountryView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry {
        country =>

          val form         = formProvider(country)
          val preparedForm = request.userAnswers.get(CurrentlyRegisteredInCountryPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, country)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry {
        country =>

          val form = formProvider(country)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, country))),

            value =>
              for {
                updatedAnswers <- updateUserAnswers(request.userAnswers, value, country)
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(CurrentlyRegisteredInCountryPage.navigate(mode, updatedAnswers))
          )
      }
  }

  private def updateUserAnswers(userAnswers: UserAnswers, currentlyRegistered: Boolean, country: Country): Future[UserAnswers] =
    if (currentlyRegistered) {
      Future.fromTry(
        userAnswers
          .set(CurrentlyRegisteredInCountryPage, true)
          .flatMap(_.set(CurrentCountryOfRegistrationPage, country))
      )
    } else {
      Future.fromTry(userAnswers.set(CurrentlyRegisteredInCountryPage, false))
    }

  private def getCountry(block: Country => Future[Result])
                        (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(AllEuDetailsQuery).map {
      details =>
        details.filter(_.vatRegistered) match {
          case oneRecord :: Nil => block(oneRecord.euCountry)
          case _                => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
