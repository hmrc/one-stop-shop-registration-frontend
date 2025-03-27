/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.rejoin

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions.*
import formats.Format.dateFormatter
import logging.Logging
import models.{RejoinMode, UserAnswers}
import models.domain.Registration
import models.requests.AuthenticatedDataRequest
import pages.{BankDetailsPage, BusinessContactDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.AllTradingNames
import services.{DateService, PeriodService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{BankDetailsSummary, BusinessContactDetailsSummary, HasTradingNameSummary, TradingNameSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.rejoin.RejoinCompleteView

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class RejoinCompleteController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          cc: AuthenticatedControllerComponents,
                                          view: RejoinCompleteView,
                                          frontendAppConfig: FrontendAppConfig,
                                          registrationConnector: RegistrationConnector,
                                          dateService: DateService,
                                          periodService: PeriodService
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData andThen cc.requireData(Some(RejoinMode))).async {
    implicit request =>
      
      for {
        externalEntryUrl <- registrationConnector.getSavedExternalEntry()
        maybeCalculatedCommencementDate <- dateService.calculateCommencementDate(request.userAnswers)
        calculatedCommencementDate = maybeCalculatedCommencementDate.getOrElse {
          val exception = new IllegalStateException("A calculated commencement date is expected, otherwise it wasn't submitted")
          logger.error(exception.getMessage, exception)
          throw exception
        }
      } yield {
        val organisationName = getOrganisationName(request.userAnswers)
        val savedUrl = externalEntryUrl.fold(_ => None, _.url)
        val periodOfFirstReturn = periodService.getFirstReturnPeriod(calculatedCommencementDate)
        val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
        val firstDayOfNextPeriod = nextPeriod.firstDay
        Ok(
          view(
            request.vrn.vrn,
            frontendAppConfig.feedbackUrl,
            savedUrl,
            yourAccountUrl = frontendAppConfig.ossYourAccountUrl,
            organisationName,
            calculatedCommencementDate.format(dateFormatter),
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            request.latestIossRegistration,
            request.numberOfIossRegistrations,
            detailList(request.registration).rows.nonEmpty
          )
        )
      }
  }

  private def getOrganisationName(answers: UserAnswers): String = {
    answers.vatInfo.flatMap { vatInfo =>
      vatInfo.organisationName.fold(vatInfo.individualName)(Some.apply)
    }.getOrElse(throw new RuntimeException("OrganisationName has not been set in answers"))
  }

  private def detailList(
                          originalRegistration: Option[Registration]
                        )(implicit request: AuthenticatedDataRequest[AnyContent]): SummaryList = {

    originalRegistration match {
      case Some(registration) =>

        SummaryListViewModel(
          rows = (
            getHasTradingNameRows(registration) ++
              getTradingNameRows(registration) ++
              getBusinessContactDetailsRows(registration) ++
              getBankDetailsRows(registration)
            ).flatten
        )
      case _ =>
        SummaryListViewModel(
          rows = Seq.empty
        )
    }
  }

  private def getHasTradingNameRows(
                                     originalRegistration: Registration
                                   )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalTradingNames = originalRegistration.tradingNames
    val amendedTradingNames = request.userAnswers.get(AllTradingNames).getOrElse(List.empty)
    val hasChangedToNo = amendedTradingNames.isEmpty && originalTradingNames.nonEmpty
    val hasChangedToYes = amendedTradingNames.nonEmpty && originalTradingNames.nonEmpty || originalTradingNames.isEmpty
    val notAmended = amendedTradingNames.nonEmpty && originalTradingNames.nonEmpty || amendedTradingNames.isEmpty && originalTradingNames.isEmpty

    if (notAmended) {
      Seq.empty
    } else if (hasChangedToNo || hasChangedToYes) {
      Seq(
        new HasTradingNameSummary().amendedAnswersRow(request.userAnswers),
      )
    } else {
      Seq.empty
    }
  }

  private def getTradingNameRows(
                                  originalRegistration: Registration
                                )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalTradingNames = originalRegistration.tradingNames
    val amendedTradingNames = request.userAnswers.get(AllTradingNames).getOrElse(List.empty)
    val addedTradingNames = amendedTradingNames.diff(originalTradingNames)
    val removedTradingNames = originalTradingNames.diff(amendedTradingNames)

    val changedTradingNames = amendedTradingNames.zip(originalTradingNames).collect {
      case (amended, original) if amended != original => amended
    } ++ amendedTradingNames.drop(originalTradingNames.size)

    val addedTradingNamesRow = if (addedTradingNames.nonEmpty) {
      request.userAnswers.set(AllTradingNames, changedTradingNames) match {
        case Success(amendedUserAnswer) =>
          Some(TradingNameSummary.amendedAnswersRow(amendedUserAnswer))

        case Failure(_) =>
          None
      }
    } else {
      None
    }

    val removedTradingNamesRow = Some(TradingNameSummary.removedAnswersRow(removedTradingNames))

    Seq(addedTradingNamesRow, removedTradingNamesRow).flatten
  }

  private def getBusinessContactDetailsRows(
                                             originalRegistration: Registration
                                           )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {
    val originalDetails = originalRegistration.contactDetails
    val amendedDetails = request.userAnswers.get(BusinessContactDetailsPage)

    Seq(
      if (amendedDetails.exists(_.fullName != originalDetails.fullName)) {
        BusinessContactDetailsSummary.amendedContactNameRow(request.userAnswers)
      } else {
        None
      },

      if (amendedDetails.exists(_.telephoneNumber != originalDetails.telephoneNumber)) {
        BusinessContactDetailsSummary.amendedTelephoneNumberRow(request.userAnswers)
      } else {
        None
      },

      if (amendedDetails.exists(_.emailAddress != originalDetails.emailAddress)) {
        BusinessContactDetailsSummary.amendedEmailAddressRow(request.userAnswers)
      } else {
        None
      }
    )
  }

  private def getBankDetailsRows(
                                  originalRegistration: Registration
                                )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalDetails = originalRegistration.bankDetails
    val amendedDetails = request.userAnswers.get(BankDetailsPage)

    Seq(
      if (amendedDetails.exists(_.accountName != originalDetails.accountName)) {
        BankDetailsSummary.amendedAccountNameRow(request.userAnswers)
      } else {
        None
      },

      if (amendedDetails.exists(_.bic != originalDetails.bic)) {
        BankDetailsSummary.amendedBICRow(request.userAnswers)
      } else {
        None
      },

      if (amendedDetails.exists(_.iban != originalDetails.iban)) {
        BankDetailsSummary.amendedIBANRow(request.userAnswers)
      } else {
        None
      }
    )
  }
}
