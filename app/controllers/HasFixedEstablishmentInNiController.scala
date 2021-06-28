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
import forms.HasFixedEstablishmentInNiFormProvider
import pages.HasFixedEstablishmentInNiPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.HasFixedEstablishmentInNiView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasFixedEstablishmentInNiController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: UnauthenticatedControllerComponents,
                                         formProvider: HasFixedEstablishmentInNiFormProvider,
                                         view: HasFixedEstablishmentInNiView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.identifyAndGetData {
    implicit request =>

      val preparedForm = request.userAnswers.get(HasFixedEstablishmentInNiPage) match {
        case Some(answer) => form.fill(answer)
        case None         => form
      }

      Ok(view(preparedForm))
  }

  def onSubmit: Action[AnyContent] = cc.identifyAndGetData.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(HasFixedEstablishmentInNiPage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(HasFixedEstablishmentInNiPage.navigate(value))
      )
  }
}
