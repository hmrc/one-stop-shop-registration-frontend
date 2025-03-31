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

package controllers

import base.SpecBase
import forms.BankDetailsFormProvider
import models.iossRegistration.IossEtmpDisplayRegistration
import models.{AmendMode, BankDetails, Bic, Iban, NormalMode, RejoinMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.BankDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import views.html.BankDetailsView

class BankDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new BankDetailsFormProvider()
  private val form = formProvider()

  private lazy val bankDetailsRoute = routes.BankDetailsController.onPageLoad(NormalMode).url

  private val bic = arbitrary[Bic].sample.value
  private val iban = arbitrary[Iban].sample.value
  private val bankDetails = BankDetails("account name", Some(bic), iban)
  private val userAnswers = basicUserAnswersWithVatInfo.set(BankDetailsPage, bankDetails).success.value

  private val iossEtmpDisplayRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

  "BankDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, bankDetailsRoute)

        val view = application.injector.instanceOf[BankDetailsView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, None, 0)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when an IOSS Registration is present" in {

      val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
        iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

      val iossBankDetails: BankDetails = BankDetails(
        accountName = nonExcludedIossEtmpDisplayRegistration.bankDetails.accountName,
        bic = nonExcludedIossEtmpDisplayRegistration.bankDetails.bic,
        iban = nonExcludedIossEtmpDisplayRegistration.bankDetails.iban
      )

      val updatedForm: Form[BankDetails] = form.fill(iossBankDetails)

      val application = applicationBuilder(
        userAnswers = Some(basicUserAnswersWithVatInfo),
        iossNumber = Some(iossNumber),
        iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
        numberOfIossRegistrations = 1
      ).build()

      running(application) {
        val request = FakeRequest(GET, bankDetailsRoute)

        val view = application.injector.instanceOf[BankDetailsView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          updatedForm,
          NormalMode,
          Some(nonExcludedIossEtmpDisplayRegistration),
          1
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when an excluded IOSS Registration is present" in {

      val iossBankDetails: BankDetails = BankDetails(
        accountName = iossEtmpDisplayRegistration.bankDetails.accountName,
        bic = iossEtmpDisplayRegistration.bankDetails.bic,
        iban = iossEtmpDisplayRegistration.bankDetails.iban
      )

      val updatedForm: Form[BankDetails] = form.fill(iossBankDetails)

      val application = applicationBuilder(
        userAnswers = Some(basicUserAnswersWithVatInfo),
        iossNumber = Some(iossNumber),
        iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
        numberOfIossRegistrations = 1
      ).build()

      running(application) {
        val request = FakeRequest(GET, bankDetailsRoute)

        val view = application.injector.instanceOf[BankDetailsView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          updatedForm,
          NormalMode,
          Some(iossEtmpDisplayRegistration),
          1
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when multiple IOSS Registrations are present" in {

      val iossBankDetails: BankDetails = BankDetails(
        accountName = iossEtmpDisplayRegistration.bankDetails.accountName,
        bic = iossEtmpDisplayRegistration.bankDetails.bic,
        iban = iossEtmpDisplayRegistration.bankDetails.iban
      )

      val updatedForm: Form[BankDetails] = form.fill(iossBankDetails)

      val application = applicationBuilder(
        userAnswers = Some(basicUserAnswersWithVatInfo),
        iossNumber = Some(iossNumber),
        iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
        numberOfIossRegistrations = 2
      ).build()

      running(application) {
        val request = FakeRequest(GET, bankDetailsRoute)

        val view = application.injector.instanceOf[BankDetailsView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          updatedForm,
          NormalMode,
          Some(iossEtmpDisplayRegistration),
          2
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, bankDetailsRoute)

        val view = application.injector.instanceOf[BankDetailsView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form.fill(bankDetails), NormalMode, None, 0)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankDetailsRoute)
            .withFormUrlEncodedBody(("accountName", "account name"), ("bic", bic.toString), ("iban", iban.toString))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswersWithVatInfo.set(BankDetailsPage, bankDetails).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` BankDetailsPage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request =
          FakeRequest(POST, bankDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[BankDetailsView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, None, 0)(request, messages(application)).toString
      }
    }

    Seq(AmendMode, RejoinMode).foreach { mode =>

      lazy val bankDetailsRoute = routes.BankDetailsController.onPageLoad(mode).url

      s"in $mode" - {

        s"must return OK and the correct view for a GET when an IOSS Registration is present when in $mode" in {

          val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
            iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

          val iossBankDetails: BankDetails = BankDetails(
            accountName = nonExcludedIossEtmpDisplayRegistration.bankDetails.accountName,
            bic = nonExcludedIossEtmpDisplayRegistration.bankDetails.bic,
            iban = nonExcludedIossEtmpDisplayRegistration.bankDetails.iban
          )

          val updatedForm: Form[BankDetails] = form.fill(iossBankDetails)

          val application = applicationBuilder(
            userAnswers = Some(basicUserAnswersWithVatInfo),
            iossNumber = Some(iossNumber),
            iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
            numberOfIossRegistrations = 1
          ).build()

          running(application) {
            val request = FakeRequest(GET, bankDetailsRoute)

            val view = application.injector.instanceOf[BankDetailsView]

            val result = route(application, request).value

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(
              updatedForm,
              mode,
              Some(nonExcludedIossEtmpDisplayRegistration),
              1
            )(request, messages(application)).toString
          }
        }

        s"must return OK and the correct view for a GET when an excluded IOSS Registration is present when in $mode" in {

          val iossBankDetails: BankDetails = BankDetails(
            accountName = iossEtmpDisplayRegistration.bankDetails.accountName,
            bic = iossEtmpDisplayRegistration.bankDetails.bic,
            iban = iossEtmpDisplayRegistration.bankDetails.iban
          )

          val updatedForm: Form[BankDetails] = form.fill(iossBankDetails)

          val application = applicationBuilder(
            userAnswers = Some(basicUserAnswersWithVatInfo),
            iossNumber = Some(iossNumber),
            iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
            numberOfIossRegistrations = 1
          ).build()

          running(application) {
            val request = FakeRequest(GET, bankDetailsRoute)

            val view = application.injector.instanceOf[BankDetailsView]

            val result = route(application, request).value

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(
              updatedForm,
              mode,
              Some(iossEtmpDisplayRegistration),
              1
            )(request, messages(application)).toString
          }
        }

        s"must return OK and the correct view for a GET when multiple IOSS Registrations are present when in $mode" in {

          val iossBankDetails: BankDetails = BankDetails(
            accountName = iossEtmpDisplayRegistration.bankDetails.accountName,
            bic = iossEtmpDisplayRegistration.bankDetails.bic,
            iban = iossEtmpDisplayRegistration.bankDetails.iban
          )

          val updatedForm: Form[BankDetails] = form.fill(iossBankDetails)

          val application = applicationBuilder(
            userAnswers = Some(basicUserAnswersWithVatInfo),
            iossNumber = Some(iossNumber),
            iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
            numberOfIossRegistrations = 2
          ).build()

          running(application) {
            val request = FakeRequest(GET, bankDetailsRoute)

            val view = application.injector.instanceOf[BankDetailsView]

            val result = route(application, request).value

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(
              updatedForm,
              mode,
              Some(iossEtmpDisplayRegistration),
              2
            )(request, messages(application)).toString
          }
        }
      }
    }
  }
}
