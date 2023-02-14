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

package controllers

import config.Constants
import controllers.actions._
import forms.AddWebsiteFormProvider
import models.Mode
import models.requests.AuthenticatedDataRequest
import pages.AddWebsitePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.DeriveNumberOfWebsites
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.WebsiteSummary
import views.html.AddWebsiteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddWebsiteController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      cc: AuthenticatedControllerComponents,
                                      formProvider: AddWebsiteFormProvider,
                                      view: AddWebsiteView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getNumberOfWebsites {
        number =>
          val canAddWebsites = number < Constants.maxWebsites
          Future.successful(Ok(view(form, mode, WebsiteSummary.addToListRows(request.userAnswers, mode), canAddWebsites)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getNumberOfWebsites {
        number =>
          val canAddWebsites = number < Constants.maxWebsites

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, mode, WebsiteSummary.addToListRows(request.userAnswers, mode), canAddWebsites))
              ),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddWebsitePage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(AddWebsitePage.navigate(mode, updatedAnswers))
          )
      }
  }

  private def getNumberOfWebsites(block: Int => Future[Result])
                                 (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfWebsites).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
