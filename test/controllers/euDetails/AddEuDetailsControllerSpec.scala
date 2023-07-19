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

package controllers.euDetails

import base.SpecBase
import controllers.routes
import controllers.euDetails.{routes => euDetailsRoutes}
import controllers.amend.{routes => amendRoutes}
import connectors.RegistrationConnector
import forms.euDetails.AddEuDetailsFormProvider
import models.euDetails.{EuConsumerSalesMethod, EuOptionalDetails, RegistrationType}
import models.{AmendMode, CheckMode, Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails._
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import testutils.RegistrationData
import viewmodels.checkAnswers.euDetails.EuDetailsSummary
import views.html.euDetails.{AddEuDetailsView, PartOfVatGroupAddEuDetailsView}

import scala.concurrent.Future

class AddEuDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddEuDetailsFormProvider()
  private val form = formProvider()
  private val countryIndex = Index(0)
  private val country = Country.euCountries.head

  private lazy val addEuVatDetailsRoute = euDetailsRoutes.AddEuDetailsController.onPageLoad(NormalMode).url
  private lazy val addEuVatDetailsAmendRoute = euDetailsRoutes.AddEuDetailsController.onPageLoad(AmendMode).url
  private def addEuVatDetailsPostRoute(prompt: Boolean = false) = euDetailsRoutes.AddEuDetailsController.onSubmit(NormalMode, prompt).url
  private def addEuVatDetailsPostAmendRoute(prompt: Boolean = false) = euDetailsRoutes.AddEuDetailsController.onSubmit(AmendMode, prompt).url

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private val baseAnswers =
    basicUserAnswersWithVatInfo
      .set(TaxRegisteredInEuPage, true).success.value
      .set(EuCountryPage(countryIndex), country).success.value
      .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
      .set(RegistrationTypePage(countryIndex), RegistrationType.TaxId).success.value
      .set(EuTaxReferencePage(countryIndex), "12345678").success.value
      .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value
      .set(EuSendGoodsAddressPage(countryIndex), arbitraryInternationalAddress.arbitrary.sample.value).success.value

  private val incompleteAnswers =
    basicUserAnswersWithVatInfo
      .set(EuCountryPage(countryIndex), country).success.value

  private val incompleteAnswersPartOfVatGroup =
    basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
      .set(EuCountryPage(countryIndex), country).success.value

  "AddEuDetails Controller" - {

    "must return OK and the correct view for a GET when answers are complete and user is not part of Vat Group" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        val view                    = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.countryAndVatNumberList(baseAnswers, NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when answers are complete and user is part of Vat Group" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
        .set(EuVatNumberPage(countryIndex), "12345").success.value
      )).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        val view                    = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.countryAndVatNumberList(baseAnswers.set(EuVatNumberPage(countryIndex), "12345").success.value, NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when answers are incomplete" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        val view                    = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.countryAndVatNumberList(incompleteAnswers, NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true,
          Seq(EuOptionalDetails(country, Some(true), None, None, None, None, None, None, None, None, None))
        )(request, implicitly).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddEuDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val view                    = application.injector.instanceOf[AddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must not be view(form.fill(true), NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(AddEuDetailsPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AddEuDetailsPage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and user is not part of vat group" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view                    = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.countryAndVatNumberList(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and user is part of vat group" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
        .set(EuVatNumberPage(countryIndex), "12345").success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view                    = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.countryAndVatNumberList(baseAnswers.set(EuVatNumberPage(countryIndex), "12345").success.value, NormalMode)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must refresh the page for a POST if answers are incomplete and the prompt has not been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.AddEuDetailsController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Sells Goods to EU Consumers page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.SellsGoodsToEUConsumersController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to Sells Goods to EU Consumer Method page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to Registration Type page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.RegistrationTypeController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to VAT Number page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.values.head).success.value
        .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.EuVatNumberController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to EU Tax Reference Number page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
        .set(RegistrationTypePage(countryIndex), RegistrationType.TaxId).success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.EuTaxReferenceController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to Fixed Establishment Trading Name page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
        .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value
        .set(EuVatNumberPage(countryIndex), "").success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to Fixed Establishment Address page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
        .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value
        .set(EuVatNumberPage(countryIndex), "123456789").success.value
        .set(FixedEstablishmentTradingNamePage(countryIndex), "Foo").success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.FixedEstablishmentAddressController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to EU Send Goods Trading Name page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
        .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value
        .set(EuVatNumberPage(countryIndex), "").success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to EU Send Goods Address page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
        .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value
        .set(EuVatNumberPage(countryIndex), "123456789").success.value
        .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.EuSendGoodsAddressController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to VAT Registered page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.VatRegisteredController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "must redirect to EU VAT Number page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers
        .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value
        .set(VatRegisteredPage(countryIndex), true).success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual euDetailsRoutes.EuVatNumberController.onPageLoad(CheckMode, countryIndex).url
      }
    }

    "when part of VAT group is true" - {

      "must redirect to Cannot Add Country page for a POST if answer is incomplete and the prompt has been shown" in {

        val application = applicationBuilder(userAnswers = Some(incompleteAnswersPartOfVatGroup
          .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
          .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
        )).build()

        running(application) {
          val request =
            FakeRequest(POST, addEuVatDetailsPostRoute(true))
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual euDetailsRoutes.CannotAddCountryController.onPageLoad(CheckMode, countryIndex).url
        }
      }
    }

    "in AmendMode" - {

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, addEuVatDetailsAmendRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a GET if user answers are empty" in {

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .build()

        running(application) {
          val request = FakeRequest(GET, addEuVatDetailsAmendRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, addEuVatDetailsPostAmendRoute())
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }

    }

  }
}
