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
import forms.previousRegistrations.PreviousOssNumberFormProvider
import models.previousRegistrations.{PreviousSchemeNumbers, PreviousSchemeHintText}
import models.requests.AuthenticatedDataRequest
import models.{Country, CountryWithValidationDetails, Index, Mode, PreviousScheme}
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration.AllPreviousSchemesForCountryWithOptionalVatNumberQuery
import services.CoreRegistrationValidationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.previousRegistrations.PreviousOssNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousOssNumberController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             cc: AuthenticatedControllerComponents,
                                             coreRegistrationValidationService: CoreRegistrationValidationService,
                                             formProvider: PreviousOssNumberFormProvider,
                                             view: PreviousOssNumberView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(countryIndex) {
        country =>

          val previousSchemeHintText = determinePreviousSchemeHintText(countryIndex, schemeIndex)

          val form = formProvider(country)

          val preparedForm = request.userAnswers.get(PreviousOssNumberPage(countryIndex, schemeIndex)) match {
            case None => form
            case Some(value) => form.fill(value.previousSchemeNumber)
          }
          CountryWithValidationDetails.euCountriesWithVRNValidationRules.filter(_.country.code == country.code).head match {
            case countryWithValidationDetails =>
              Future.successful(Ok(view(preparedForm, mode, countryIndex, schemeIndex, countryWithValidationDetails, previousSchemeHintText)))
          }
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(countryIndex) {
        country =>

          val previousSchemeHintText = determinePreviousSchemeHintText(countryIndex, schemeIndex)

          val form = formProvider(country)

          form.bindFromRequest().fold(
            formWithErrors =>
              CountryWithValidationDetails.euCountriesWithVRNValidationRules.filter(_.country.code == country.code).head match {
                case countryWithValidationDetails =>
                  Future.successful(BadRequest(view(formWithErrors, mode, countryIndex, schemeIndex, countryWithValidationDetails, previousSchemeHintText)))
              },

            value => {
              val previousScheme = if (value.startsWith("EU")) {
                PreviousScheme.OSSNU
              } else {
                PreviousScheme.OSSU
              }

              coreRegistrationValidationService.searchScheme(
                searchNumber = value,
                previousScheme = previousScheme,
                intermediaryNumber = None,
                countryCode = country.code
              ).flatMap {
                case Some(activeMatch) if coreRegistrationValidationService.isActiveTrader(activeMatch) =>
                  Future.successful(Redirect(controllers.previousRegistrations.routes.SchemeStillActiveController.onPageLoad()))
                case Some(activeMatch) if coreRegistrationValidationService.isQuarantinedTrader(activeMatch) =>
                  Future.successful(Redirect(controllers.previousRegistrations.routes.SchemeQuarantinedController.onPageLoad()))
                case matchValue =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(
                      PreviousOssNumberPage(countryIndex, schemeIndex),
                      PreviousSchemeNumbers(value, None)
                    ))
                    updatedAnswersWithScheme <- Future.fromTry(updatedAnswers.set(
                      PreviousSchemePage(countryIndex, schemeIndex),
                      previousScheme
                    ))
                    _ <- cc.sessionRepository.set(updatedAnswersWithScheme)
                  } yield Redirect(PreviousOssNumberPage(countryIndex, schemeIndex).navigate(mode, updatedAnswersWithScheme))
              }
            }
          )
      }

  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(PreviousEuCountryPage(index)).map {
      country =>
        block(country)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  private def determinePreviousSchemeHintText(countryIndex: Index,
                                              schemeIndex: Index
                                             )(implicit request: AuthenticatedDataRequest[AnyContent]): PreviousSchemeHintText = {

    getPreviousSchemeHintText(request.userAnswers.get(PreviousSchemePage(countryIndex, schemeIndex)),
      checkPreviousSchemeForCountry(countryIndex))
  }

  private def checkPreviousSchemeForCountry(index: Index)
                                           (implicit request: AuthenticatedDataRequest[AnyContent]): Option[PreviousScheme] = {
    request.userAnswers
      .get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(index)) match {
      case Some(previousSchemesDetails) =>
        previousSchemesDetails.flatMap(_.previousScheme).headOption
      case None =>
        None
    }
  }

  private def getPreviousSchemeHintText(currentPreviousScheme: Option[PreviousScheme],
                                        otherSelectedScheme: Option[PreviousScheme]): PreviousSchemeHintText = {
    currentPreviousScheme match {
      case Some(value) => value match {
        case PreviousScheme.OSSNU => PreviousSchemeHintText.OssNonUnion
        case PreviousScheme.OSSU => PreviousSchemeHintText.OssUnion
        case _ => PreviousSchemeHintText.Both
      }
      case None => otherSelectedScheme match {
        case Some(value) => value match {
          case PreviousScheme.OSSNU => PreviousSchemeHintText.OssUnion
          case PreviousScheme.OSSU => PreviousSchemeHintText.OssNonUnion
          case _ => PreviousSchemeHintText.Both
        }
        case None => PreviousSchemeHintText.Both
      }
    }

  }
}
