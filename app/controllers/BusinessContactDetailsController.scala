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
import forms.BusinessContactDetailsFormProvider
import models.Mode
import navigation.Navigator
import pages.BusinessContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessContactDetailsController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      cc: AuthenticatedControllerComponents,
                                      navigator: Navigator,
                                      formProvider: BusinessContactDetailsFormProvider,
                                      view: BusinessContactDetailsView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(BusinessContactDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessContactDetailsPage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(BusinessContactDetailsPage, mode, updatedAnswers))
      )
  }
}
