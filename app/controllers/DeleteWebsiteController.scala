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
import forms.DeleteWebsiteFormProvider
import models.requests.DataRequest

import javax.inject.Inject
import models.{Index, Mode}
import navigation.Navigator
import pages.{DeleteWebsitePage, WebsitePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeleteWebsiteView

import scala.concurrent.{ExecutionContext, Future}

class DeleteWebsiteController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             sessionRepository: SessionRepository,
                                             navigator: Navigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: DeleteWebsiteFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: DeleteWebsiteView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getWebsite(index) {
        website =>
          Future.successful(Ok(view(form, mode, index, website)))
      }

  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getWebsite(index) {
        website =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, index, website))),

            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(WebsitePage(index)))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(DeleteWebsitePage(index), mode, updatedAnswers))
              } else {
                Future.successful(Redirect(navigator.nextPage(DeleteWebsitePage(index), mode, request.userAnswers)))
              }
          )
      }
  }

  private def getWebsite(index: Index)
                        (block: String => Future[Result])
                        (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(WebsitePage(index)).map {
      name =>
        block(name)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
}
