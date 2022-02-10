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

package controllers.euDetails

import controllers.actions._
import forms.euDetails.DeleteEuDetailsFormProvider
import models.euDetails.{EuDetails, EuOptionalDetails}
import models.requests.AuthenticatedDataRequest
import models.{Index, Mode}
import pages.euDetails.DeleteEuDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{EuDetailsQuery, EuOptionalDetailsQuery}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.DeleteEuDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteEuDetailsController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           cc: AuthenticatedControllerComponents,
                                           formProvider: DeleteEuDetailsFormProvider,
                                           view: DeleteEuDetailsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getEuVatDetails(index) {
        details =>

          val form = formProvider(details.euCountry.name)

          Future.successful(Ok(view(form, mode, index, details.euCountry.name)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getEuVatDetails(index) {
        details =>

          val form = formProvider(details.euCountry.name)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, index, details.euCountry.name))),

            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(EuDetailsQuery(index)))
                  _              <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(DeleteEuDetailsPage(index).navigate(mode, updatedAnswers))
              } else {
                Future.successful(Redirect(DeleteEuDetailsPage(index).navigate(mode, request.userAnswers)))
              }
          )
      }
  }


  private def getEuVatDetails(index: Index)
                             (block: EuOptionalDetails => Future[Result])
                             (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(EuOptionalDetailsQuery(index)).map {
      details =>
        block(details)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
