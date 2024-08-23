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

package controllers.amend

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions._
import models.UserAnswers
import models.domain.Registration
import models.requests.AuthenticatedDataRequest
import pages.{BankDetailsPage, BusinessContactDetailsPage, DateOfFirstSalePage, IsOnlineMarketplacePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.previousRegistration.AllPreviousRegistrationsQuery
import queries.{AllEuOptionalDetailsQuery, AllTradingNames, AllWebsites}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.euDetails.{EuDetailsSummary, TaxRegisteredInEuSummary}
import viewmodels.checkAnswers._
import viewmodels.checkAnswers.previousRegistrations.{PreviousRegistrationSummary, PreviouslyRegisteredSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.amend.AmendCompleteView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AmendCompleteController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         view: AmendCompleteView,
                                         frontendAppConfig: FrontendAppConfig,
                                         registrationConnector: RegistrationConnector,
                                         commencementDateSummary: CommencementDateSummary,
                                         clock: Clock
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData andThen cc.requireData(None)).async {
    implicit request => {

      for {
        externalEntryUrl <- registrationConnector.getSavedExternalEntry()
        originalRegistration <- registrationConnector.getRegistration()(hc)
        cds <- commencementDateSummary.row(request.userAnswers)
      } yield {
        val organisationName = getOrganisationName(request.userAnswers)
        val savedUrl = externalEntryUrl.fold(_ => None, _.url)
        val list: SummaryList = detailList(cds, originalRegistration, request.userAnswers)
        Ok(
          view(
            request.vrn,
            frontendAppConfig.feedbackUrl,
            savedUrl,
            frontendAppConfig.ossYourAccountUrl,
            organisationName.toString,
            list
          )
        )
      }
    }
  }

  private def getOrganisationName(answers: UserAnswers): Option[String] =
    answers.vatInfo match {
      case Some(vatInfo) if vatInfo.organisationName.isDefined => vatInfo.organisationName
      case Some(vatInfo) if vatInfo.individualName.isDefined => vatInfo.individualName
      case _ => None
    }

  private def detailList(cds: SummaryListRow, originalRegistration: Option[Registration], userAnswers: UserAnswers)
                        (implicit request: AuthenticatedDataRequest[AnyContent]) = {

    SummaryListViewModel(
      rows = (
        getHasTradingNameRows(originalRegistration, userAnswers) ++
        getTradingNameRows(originalRegistration, userAnswers) ++
        getHasSalesRows(originalRegistration, userAnswers) ++
        getSalesRows(cds, originalRegistration, userAnswers) ++
        getHasPreviouslyRegistered(originalRegistration, userAnswers)++
        getPreviouslyRegisteredRows(originalRegistration, userAnswers) ++
        getHasRegisteredInEuRows(originalRegistration, userAnswers) ++
        getRegisteredInEuRows(originalRegistration, userAnswers) ++
        getIsOnlineMarketPlace(originalRegistration, userAnswers) ++
        getHasWebsiteRows(originalRegistration, userAnswers) ++
        getWebsiteRows(originalRegistration, userAnswers) ++
        getBusinessContactDetailsRows(originalRegistration, userAnswers) ++
        getBankDetailsRows(originalRegistration, userAnswers)
      ).flatten
    )
  }

  private def getHasTradingNameRows(
                                     originalRegistration: Option[Registration],
                                     userAnswers: UserAnswers
                                   )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalTradingNames = originalRegistration.map(_.tradingNames).getOrElse(List.empty)
    val amendedTradingNames = userAnswers.get(AllTradingNames).getOrElse(List.empty)
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
                                  originalRegistration: Option[Registration],
                                  userAnswers: UserAnswers
                                )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalTradingNames = originalRegistration.map(_.tradingNames).getOrElse(List.empty)
    val amendedTradingNames = userAnswers.get(AllTradingNames).getOrElse(List.empty)
    val addedTradingNames = amendedTradingNames.diff(originalTradingNames)
    val removedTradingNames = originalTradingNames.diff(amendedTradingNames)

    val changedTradingNames = amendedTradingNames.zip(originalTradingNames).collect {
      case (amended, original) if amended != original => amended
    } ++ amendedTradingNames.drop(originalTradingNames.size)

    val addedTradingNamesRow = if (addedTradingNames.nonEmpty) {
      userAnswers.set(AllTradingNames, changedTradingNames) match {
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

  private def getHasSalesRows(
                               originalRegistration: Option[Registration],
                               userAnswers: UserAnswers
                             )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalDateOfFirstSale = originalRegistration.flatMap(_.dateOfFirstSale).getOrElse(List.empty)
    val amendedDateOfFirstSale = userAnswers.get(DateOfFirstSalePage).getOrElse(List.empty)
    val hasChangedToNo = amendedDateOfFirstSale.toString.isEmpty && originalDateOfFirstSale.toString.nonEmpty
    val hasChangedToYes = amendedDateOfFirstSale.toString.nonEmpty && originalDateOfFirstSale.toString.nonEmpty || originalDateOfFirstSale.toString.isEmpty
    val hasChangedDateOfFirstSale = originalDateOfFirstSale != amendedDateOfFirstSale
    val changedAnswer = hasChangedToNo || hasChangedToYes

    if (changedAnswer && hasChangedDateOfFirstSale) {
      Seq(
        HasMadeSalesSummary.amendedAnswersRow(request.userAnswers),
      )
    } else {
      Seq.empty
    }
  }

  private def getSalesRows(
                            cds: SummaryListRow,
                            originalRegistration: Option[Registration],
                            userAnswers: UserAnswers
                          )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalDateOfFirstSale = originalRegistration.flatMap(_.dateOfFirstSale).getOrElse(List.empty)
    val amendedDateOfFirstSale = userAnswers.get(DateOfFirstSalePage).getOrElse(List.empty)
    val hasChangedDateOfFirstSale = originalDateOfFirstSale != amendedDateOfFirstSale

    if (hasChangedDateOfFirstSale) {
      Seq(
        DateOfFirstSaleSummary.amendedAnswersRow(request.userAnswers),
        Some(cds)
      )
    } else {
      Seq.empty
    }

  }

  private def getHasPreviouslyRegistered(
                                          originalRegistration: Option[Registration],
                                          userAnswers: UserAnswers
                                        )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalPreviouslyRegDetails = originalRegistration.map(_.previousRegistrations.map(_.country)).getOrElse(List.empty)
    val amendedPreviouslyRegDetails = userAnswers.get(AllPreviousRegistrationsQuery).map(_.map(_.previousEuCountry)).getOrElse(List.empty)
    val hasChangedToNo = amendedPreviouslyRegDetails.isEmpty && originalPreviouslyRegDetails.nonEmpty
    val hasChangedToYes = amendedPreviouslyRegDetails.isEmpty && originalPreviouslyRegDetails.nonEmpty || originalPreviouslyRegDetails.isEmpty
    val notAmended = originalPreviouslyRegDetails.nonEmpty && amendedPreviouslyRegDetails.nonEmpty ||
      originalPreviouslyRegDetails.isEmpty && amendedPreviouslyRegDetails.isEmpty

    val changedAnswer = hasChangedToNo || hasChangedToYes

    if (notAmended) {
      Seq.empty
    } else if (changedAnswer) {
      Seq(
        PreviouslyRegisteredSummary.amendedAnswersRow(request.userAnswers),
      )
    } else {
      Seq.empty
    }
  }

  private def getPreviouslyRegisteredRows(
                                           originalRegistration: Option[Registration],
                                           userAnswers: UserAnswers
                                         )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalPreviouslyRegisteredDetails = originalRegistration.map(_.previousRegistrations.map(_.country)).getOrElse(List.empty)
    val amendedPreviouslyRegisteredDetails = userAnswers.get(AllPreviousRegistrationsQuery).map(_.map(_.previousEuCountry)).getOrElse(List.empty)

    val newPreviouslyRegisteredCountry = amendedPreviouslyRegisteredDetails.filterNot { addedCountry =>
      originalPreviouslyRegisteredDetails.contains(addedCountry)
    }

    if (newPreviouslyRegisteredCountry.nonEmpty) {
      val addedDetails = userAnswers.get(AllPreviousRegistrationsQuery).getOrElse(List.empty)
        .filter(details => newPreviouslyRegisteredCountry.contains(details.previousEuCountry))

      request.userAnswers.set(AllPreviousRegistrationsQuery, addedDetails) match {
        case Success(amendedUserAnswer) =>
          Seq(
            PreviousRegistrationSummary.amendedAnswersRow(amendedUserAnswer)
          )
        case Failure(_) =>
          Seq.empty
      }
    } else {
      Seq.empty
    }
  }

  private def getHasRegisteredInEuRows(
                                        originalRegistration: Option[Registration],
                                        userAnswers: UserAnswers
                                      )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalEuDetails = originalRegistration.map(_.euRegistrations.map(_.country)).getOrElse(List.empty)
    val amendedEuDetails = userAnswers.get(AllEuOptionalDetailsQuery).map(_.map(_.euCountry)).getOrElse(List.empty)
    val hasChangedToNo = amendedEuDetails.isEmpty && originalEuDetails.nonEmpty
    val hasChangedToYes = amendedEuDetails.nonEmpty && originalEuDetails.nonEmpty || originalEuDetails.isEmpty
    val notAmended = originalEuDetails.nonEmpty && amendedEuDetails.nonEmpty || originalEuDetails.isEmpty && amendedEuDetails.isEmpty

    if (notAmended) {
      Seq.empty
    } else if (hasChangedToNo || hasChangedToYes) {
      Seq(
        TaxRegisteredInEuSummary.amendedAnswersRow(request.userAnswers)
      )
    } else {
      Seq.empty
    }
  }

  private def getRegisteredInEuRows(
                                     originalRegistration: Option[Registration],
                                     userAnswers: UserAnswers
                                   )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalEuDetails = originalRegistration.map(_.euRegistrations.map(_.country)).getOrElse(List.empty)
    val amendedEuDetails = userAnswers.get(AllEuOptionalDetailsQuery).map(_.map(_.euCountry)).getOrElse(List.empty)
    val addedEuDetails = amendedEuDetails.diff(originalEuDetails)
    val removedEuDetails = originalEuDetails.diff(amendedEuDetails)

    val newOrChangedEuDetails = amendedEuDetails.filterNot { amendedCountry =>
      originalEuDetails.contains(amendedCountry)
    }

    val addedEuDetailsRow = if (addedEuDetails.nonEmpty) {
      val changedDetails = userAnswers.get(AllEuOptionalDetailsQuery).getOrElse(List.empty)
        .filter(details => newOrChangedEuDetails.contains(details.euCountry))

      request.userAnswers.set(AllEuOptionalDetailsQuery, changedDetails) match {
        case Success(amendedUserAnswers) =>
          Some(EuDetailsSummary.amendedAnswersRow(amendedUserAnswers))

        case Failure(_) =>
          None
      }
    } else {
      None
    }


    val removedEuDetailsRow = Some(EuDetailsSummary.removedAnswersRow(removedEuDetails))

    Seq(addedEuDetailsRow, removedEuDetailsRow).flatten
  }

  private def getIsOnlineMarketPlace(
                                      originalRegistration: Option[Registration],
                                      userAnswers: UserAnswers
                                    )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalIsOMPAnswer = originalRegistration.map(_.isOnlineMarketplace)
    val amendedIsOMPAnswer = userAnswers.get(IsOnlineMarketplacePage)

    if (originalIsOMPAnswer != amendedIsOMPAnswer) {
      Seq(
        IsOnlineMarketplaceSummary.amendedAnswerRow(request.userAnswers)
      )
    } else {
      Seq.empty
    }
  }

  private def getHasWebsiteRows(
                                 originalRegistration: Option[Registration],
                                 userAnswers: UserAnswers
                               )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalWebsiteAnswers = originalRegistration.map(_.websites).getOrElse(List.empty)
    val amendedWebsitesAnswers = userAnswers.get(AllWebsites).getOrElse(List.empty)
    val hasChangedToNo = amendedWebsitesAnswers.isEmpty && originalWebsiteAnswers.nonEmpty
    val hasChangedToYes = amendedWebsitesAnswers.nonEmpty && originalWebsiteAnswers.nonEmpty || originalWebsiteAnswers.isEmpty
    val notAmended = amendedWebsitesAnswers.nonEmpty && originalWebsiteAnswers.nonEmpty || amendedWebsitesAnswers.isEmpty && originalWebsiteAnswers.isEmpty

     if (notAmended) {
       Seq.empty
     } else if (hasChangedToNo || hasChangedToYes) {
      Seq(
        HasWebsiteSummary.amendedAnswersRow(request.userAnswers)
      )
    } else {
      Seq.empty
    }

  }

  private def getWebsiteRows(
                              originalRegistration: Option[Registration],
                                 userAnswers: UserAnswers
                               )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalWebsiteAnswers = originalRegistration.map(_.websites).getOrElse(List.empty)
    val amendedWebsitesAnswers = userAnswers.get(AllWebsites).getOrElse(List.empty)
    val addedWebsites = amendedWebsitesAnswers.diff(originalWebsiteAnswers)
    val removedWebsites = originalWebsiteAnswers.diff(amendedWebsitesAnswers)

    val changedWebsiteAnswers = amendedWebsitesAnswers.zip(originalWebsiteAnswers).collect {
      case (amended, original) if amended != original => amended
    } ++ amendedWebsitesAnswers.drop(originalWebsiteAnswers.size)

    val addedWebsiteRow = if (addedWebsites.nonEmpty) {
      userAnswers.set(AllWebsites, changedWebsiteAnswers) match {
        case Success(amendedUserAnswers) =>
          Some(WebsiteSummary.amendedAnswersRow(amendedUserAnswers))
        case Failure(_) =>
          None
      }
    } else {
      None
    }

    val removedWebsiteRow = Some(WebsiteSummary.removedWebsiteRow(removedWebsites))

    Seq(addedWebsiteRow, removedWebsiteRow).flatten

  }

  private def getBusinessContactDetailsRows(originalRegistration: Option[Registration],
                                            userAnswers: UserAnswers
                                           )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalDetails = originalRegistration.map(_.contactDetails)
    val amendedDetails = userAnswers.get(BusinessContactDetailsPage)

    Seq(
      if (originalDetails.map(_.fullName) != amendedDetails.map(_.fullName)) {
        BusinessContactDetailsSummary.amendedContactNameRow(userAnswers)
      } else {
        None
      },

      if (originalDetails.map(_.telephoneNumber) != amendedDetails.map(_.telephoneNumber)) {
        BusinessContactDetailsSummary.amendedTelephoneNumberRow(userAnswers)
      } else {
        None
      },

      if (originalDetails.map(_.emailAddress) != amendedDetails.map(_.emailAddress)) {
        BusinessContactDetailsSummary.amendedEmailAddressRow(userAnswers)
      } else {
        None
      }
    )
  }

  private def getBankDetailsRows(
                                  originalRegistration: Option[Registration],
                                  userAnswers: UserAnswers
                                )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalDetails = originalRegistration.map(_.bankDetails)
    val amendedDetails = userAnswers.get(BankDetailsPage)

    Seq(
      if (originalDetails.map(_.accountName) != amendedDetails.map(_.accountName)) {
        BankDetailsSummary.amendedAccountNameRow(userAnswers)
      } else {
        None
      },

      if (originalDetails.map(_.bic) != amendedDetails.map(_.bic)) {
        BankDetailsSummary.amendedBICRow(userAnswers)
      } else {
        None
      },

      if (originalDetails.map(_.iban) != amendedDetails.map(_.iban)) {
        BankDetailsSummary.amendedIBANRow(userAnswers)
      } else {
        None
      }
    )
  }
}
