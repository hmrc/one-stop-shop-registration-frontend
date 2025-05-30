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

package controllers.euDetails

import base.SpecBase
import controllers.amend.routes as amendRoutes
import controllers.euDetails.routes as euDetailsRoutes
import controllers.routes
import forms.euDetails.AddEuDetailsFormProvider
import models.euDetails.{EuConsumerSalesMethod, EuOptionalDetails, RegistrationType}
import models.{AmendMode, CheckMode, Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.euDetails.EuDetailsSummary
import views.html.euDetails.{AddEuDetailsView, PartOfVatGroupAddEuDetailsView}

class AddEuDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddEuDetailsFormProvider()
  private val form = formProvider()
  private val countryIndex = Index(0)
  private val country = Country.euCountries.head

  private lazy val addEuVatDetailsRoute = euDetailsRoutes.AddEuDetailsController.onPageLoad(NormalMode).url
  private lazy val addEuVatDetailsAmendRoute = euDetailsRoutes.AddEuDetailsController.onPageLoad(AmendMode).url

  private def addEuVatDetailsPostRoute(prompt: Boolean = false) = euDetailsRoutes.AddEuDetailsController.onSubmit(NormalMode, prompt).url
  
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

        val view = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list = EuDetailsSummary.countryAndVatNumberList(baseAnswers, NormalMode)

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when answers are complete and user is part of Vat Group" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
        .set(EuVatNumberPage(countryIndex), "ATU12345678").success.value
      )).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list = EuDetailsSummary.countryAndVatNumberList(baseAnswers.set(EuVatNumberPage(countryIndex), "ATU12345678").success.value, NormalMode)

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when answers are incomplete" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list = EuDetailsSummary.countryAndVatNumberList(incompleteAnswers, NormalMode)

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, list, canAddCountries = true,
          Seq(EuOptionalDetails(country, Some(true), None, None, None, None, None, None, None, None, None))
        )(request, implicitly).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddEuDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val view = application.injector.instanceOf[AddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list = EuDetailsSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) must not be view(form.fill(true), NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` AddEuDetailsPage.navigate(NormalMode, expectedAnswers).url
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

        val view = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list = EuDetailsSummary.countryAndVatNumberList(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and user is part of vat group" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
        .set(EuVatNumberPage(countryIndex), "ATU12345678").success.value
      )).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartOfVatGroupAddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list = EuDetailsSummary.countryAndVatNumberList(baseAnswers.set(EuVatNumberPage(countryIndex), "ATU12345678").success.value, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must redirect to CheckYourAnswers for a GET if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must refresh the page for a POST if answers are incomplete and the prompt has not been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.AddEuDetailsController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Sells Goods to EU Consumers page for a POST if answer is incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.SellsGoodsToEUConsumersController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.RegistrationTypeController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.EuVatNumberController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.EuTaxReferenceController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.FixedEstablishmentAddressController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.EuSendGoodsAddressController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.VatRegisteredController.onPageLoad(CheckMode, countryIndex).url
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

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` euDetailsRoutes.EuVatNumberController.onPageLoad(CheckMode, countryIndex).url
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

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` euDetailsRoutes.CannotAddCountryController.onPageLoad(CheckMode, countryIndex).url
        }
      }
    }

    "in AmendMode" - {

      "must redirect to resolve missing answers for a GET if user answers are empty" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .build()

        running(application) {
          val request = FakeRequest(GET, addEuVatDetailsAmendRoute)

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.ChangeYourRegistrationController.onPageLoad().url
        }
      }
    }
  }
}

