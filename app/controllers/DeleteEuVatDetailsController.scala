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
import forms.DeleteEuVatDetailsFormProvider
import models.requests.DataRequest

import javax.inject.Inject
import models.{EuVatDetails, Index, Mode}
import navigation.Navigator
import pages.DeleteEuVatDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.EuVatDetailsQuery
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeleteEuVatDetailsView

import scala.concurrent.{ExecutionContext, Future}

class DeleteEuVatDetailsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         navigator: Navigator,
                                         formProvider: DeleteEuVatDetailsFormProvider,
                                         view: DeleteEuVatDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getEuVatDetails(index) {
        details =>
          Future.successful(Ok(view(form, mode, index, details)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getEuVatDetails(index) {
        details =>

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, index, details))),

            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(EuVatDetailsQuery(index)))
                  _              <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(DeleteEuVatDetailsPage(index), mode, updatedAnswers))
              } else {
                Future.successful(Redirect(navigator.nextPage(DeleteEuVatDetailsPage(index), mode, request.userAnswers)))
              }
          )
      }
  }


  private def getEuVatDetails(index: Index)
                             (block: EuVatDetails => Future[Result])
                             (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(EuVatDetailsQuery(index)).map {
      details =>
        block(details)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
