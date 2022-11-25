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
import forms.previousRegistrations.AddPreviousRegistrationFormProvider
import models.previousRegistrations.PreviousRegistrationDetailsWithOptionalVatNumber
import models.requests.AuthenticatedDataRequest
import models.{Country, Index, Mode}
import pages.previousRegistrations.AddPreviousRegistrationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration.DeriveNumberOfPreviousRegistrations
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CompletionChecks
import viewmodels.checkAnswers.previousRegistrations.PreviousRegistrationSummary
import views.html.previousRegistrations.AddPreviousRegistrationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddPreviousRegistrationController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: AddPreviousRegistrationFormProvider,
                                         view: AddPreviousRegistrationView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with CompletionChecks with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getNumberOfPreviousRegistrations {
        number =>

          val canAddCountries = number < Country.euCountries.size
          val previousRegistrations = PreviousRegistrationSummary.addToListRows(request.userAnswers, mode)

          withCompleteDataAsync[PreviousRegistrationDetailsWithOptionalVatNumber](
            data = getAllIncompleteDeregisteredDetails,
            onFailure = (incomplete: Seq[PreviousRegistrationDetailsWithOptionalVatNumber]) => {
              Future.successful(Ok(view(form, mode, previousRegistrations, canAddCountries, incomplete)))
            }) {
            Future.successful(Ok(view(form, mode, previousRegistrations, canAddCountries)))
          }


      }
  }

  def onSubmit(mode: Mode, incompletePromptShown: Boolean): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
          withCompleteDataAsync[PreviousRegistrationDetailsWithOptionalVatNumber](
            data = getAllIncompleteDeregisteredDetails,
            onFailure = (incomplete: Seq[PreviousRegistrationDetailsWithOptionalVatNumber]) => {
              if(incompletePromptShown) {
                firstIndexedIncompleteDeregisteredCountry(incomplete.map(_.previousEuCountry)) match {
                  case Some(incompleteCountry) =>
                      Future.successful(Redirect(routes.PreviousOssNumberController.onPageLoad(mode, Index(incompleteCountry._2), Index(0)))) // TODO incomplete checks
                  case None =>
                    Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }
              } else {
                Future.successful(Redirect(routes.AddPreviousRegistrationController.onPageLoad(mode)))
              }
            }) {
            getNumberOfPreviousRegistrations {
              number =>
                val canAddCountries = number < Country.euCountries.size
                val previousRegistrations = PreviousRegistrationSummary.addToListRows(request.userAnswers, mode)

                form.bindFromRequest().fold(
                  formWithErrors =>
                    Future.successful(BadRequest(view(formWithErrors, mode, previousRegistrations, canAddCountries))),

                  value =>
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPreviousRegistrationPage, value))
                      _ <- cc.sessionRepository.set(updatedAnswers)
                    } yield Redirect(AddPreviousRegistrationPage.navigate(mode, updatedAnswers))
                )
            }
          }
  }

  private def getNumberOfPreviousRegistrations(block: Int => Future[Result])
                                              (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfPreviousRegistrations).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
