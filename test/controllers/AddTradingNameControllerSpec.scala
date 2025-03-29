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
import controllers.amend.routes as amendRoutes
import forms.AddTradingNameFormProvider
import models.iossRegistration.IossEtmpDisplayRegistration
import models.{AmendMode, Index, NormalMode, RejoinMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AddTradingNamePage, TradingNamePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.AllTradingNames
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.TradingNameSummary
import views.html.AddTradingNameView

class AddTradingNameControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddTradingNameFormProvider()
  private val form = formProvider()
  private val baseAnswers = basicUserAnswersWithVatInfo.set(TradingNamePage(Index(0)), "foo").success.value

  private lazy val addTradingNameRoute = routes.AddTradingNameController.onPageLoad(NormalMode).url
  private lazy val addTradingNameAmendRoute = routes.AddTradingNameController.onPageLoad(AmendMode).url

  private val iossEtmpDisplayRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

  "AddTradingName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameRoute)

        val view = application.injector.instanceOf[AddTradingNameView]
        implicit val msgs: Messages = messages(application)
        val list = TradingNameSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, list, canAddTradingNames = true, None, 0)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when an IOSS Registration is present with trading names" in {

      val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
        iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

      val updatedAnswers: UserAnswers = baseAnswers
        .set(AllTradingNames, nonExcludedIossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value

      val application = applicationBuilder(
        userAnswers = Some(updatedAnswers),
        iossNumber = Some(iossNumber),
        iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
        numberOfIossRegistrations = 1
      )
        .build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameRoute)

        val view = application.injector.instanceOf[AddTradingNameView]
        implicit val msgs: Messages = messages(application)
        val list = TradingNameSummary.addToListRows(updatedAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          form,
          NormalMode,
          list,
          canAddTradingNames = true,
          Some(nonExcludedIossEtmpDisplayRegistration),
          1
        )(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when an excluded IOSS Registration is present with trading names" in {

      val updatedAnswers: UserAnswers = baseAnswers
        .set(AllTradingNames, iossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value

      val application = applicationBuilder(
        userAnswers = Some(updatedAnswers),
        iossNumber = Some(iossNumber),
        iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
        numberOfIossRegistrations = 1
      )
        .build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameRoute)

        val view = application.injector.instanceOf[AddTradingNameView]
        implicit val msgs: Messages = messages(application)
        val list = TradingNameSummary.addToListRows(updatedAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          form,
          NormalMode,
          list,
          canAddTradingNames = true,
          Some(iossEtmpDisplayRegistration),
          1
        )(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when multiple IOSS Registrations are present with trading names" in {

      val updatedAnswers: UserAnswers = baseAnswers
        .set(AllTradingNames, iossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value

      val application = applicationBuilder(
        userAnswers = Some(updatedAnswers),
        iossNumber = Some(iossNumber),
        iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
        numberOfIossRegistrations = 2
      )
        .build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameRoute)

        val view = application.injector.instanceOf[AddTradingNameView]
        implicit val msgs: Messages = messages(application)
        val list = TradingNameSummary.addToListRows(updatedAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          form,
          NormalMode,
          list,
          canAddTradingNames = true,
          Some(iossEtmpDisplayRegistration),
          2
        )(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when the maximum number of trading names have already been added" in {

      val answers =
        basicUserAnswersWithVatInfo
          .set(TradingNamePage(Index(0)), "foo").success.value
          .set(TradingNamePage(Index(1)), "foo").success.value
          .set(TradingNamePage(Index(2)), "foo").success.value
          .set(TradingNamePage(Index(3)), "foo").success.value
          .set(TradingNamePage(Index(4)), "foo").success.value
          .set(TradingNamePage(Index(5)), "foo").success.value
          .set(TradingNamePage(Index(6)), "foo").success.value
          .set(TradingNamePage(Index(7)), "foo").success.value
          .set(TradingNamePage(Index(8)), "foo").success.value
          .set(TradingNamePage(Index(9)), "foo").success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameRoute)

        val view = application.injector.instanceOf[AddTradingNameView]
        implicit val msgs: Messages = messages(application)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe`
          view(form, NormalMode, TradingNameSummary.addToListRows(answers, NormalMode), canAddTradingNames = false, None, 0)(request, implicitly).toString
      }
    }

    "must redirect to CheckYourAnswersController and the correct view for a GET when cannot derive number of trading names" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must not populate the answer on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddTradingNamePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameRoute)

        val view = application.injector.instanceOf[AddTradingNameView]
        implicit val msgs: Messages = messages(application)
        val list = TradingNameSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) must not be view(form.fill(true), NormalMode, list, canAddTradingNames = true, None, 0)(request, implicitly).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addTradingNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        val expectedAnswers = baseAnswers.set(AddTradingNamePage, true).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` AddTradingNamePage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addTradingNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddTradingNameView]
        implicit val msgs: Messages = messages(application)
        val list = TradingNameSummary.addToListRows(baseAnswers, NormalMode)


        val result = route(application, request).value
        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, list, canAddTradingNames = true, None, 0)(request, implicitly).toString
      }
    }

    "must redirect to resolve missing answers and the correct view for a GET when cannot derive number of trading names when in Amend mode" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
        .build()

      running(application) {
        val request = FakeRequest(GET, addTradingNameAmendRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` amendRoutes.ChangeYourRegistrationController.onPageLoad().url
      }
    }

    Seq(AmendMode, RejoinMode).foreach { mode =>

      lazy val addTradingNameRoute = routes.AddTradingNameController.onPageLoad(mode).url

      s"in $mode" - {

        s"must return OK and the correct view for a GET when an IOSS Registration is present with trading names when in $mode" in {

          val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
            iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

          val updatedAnswers: UserAnswers = baseAnswers
            .set(AllTradingNames, nonExcludedIossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value

          val application = applicationBuilder(
            userAnswers = Some(updatedAnswers),
            iossNumber = Some(iossNumber),
            iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
            numberOfIossRegistrations = 1
          )
            .build()

          running(application) {
            val request = FakeRequest(GET, addTradingNameRoute)

            val view = application.injector.instanceOf[AddTradingNameView]
            implicit val msgs: Messages = messages(application)
            val list = TradingNameSummary.addToListRows(updatedAnswers, mode)

            val result = route(application, request).value

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(
              form,
              mode,
              list,
              canAddTradingNames = true,
              Some(nonExcludedIossEtmpDisplayRegistration),
              1
            )(request, implicitly).toString
          }
        }

        s"must return OK and the correct view for a GET when an excluded IOSS Registration is present with trading names when in $mode" in {

          val updatedAnswers: UserAnswers = baseAnswers
            .set(AllTradingNames, iossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value

          val application = applicationBuilder(
            userAnswers = Some(updatedAnswers),
            iossNumber = Some(iossNumber),
            iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
            numberOfIossRegistrations = 1
          )
            .build()

          running(application) {
            val request = FakeRequest(GET, addTradingNameRoute)

            val view = application.injector.instanceOf[AddTradingNameView]
            implicit val msgs: Messages = messages(application)
            val list = TradingNameSummary.addToListRows(updatedAnswers, mode)

            val result = route(application, request).value

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(
              form,
              mode,
              list,
              canAddTradingNames = true,
              Some(iossEtmpDisplayRegistration),
              1
            )(request, implicitly).toString
          }
        }

        s"must return OK and the correct view for a GET when multiple IOSS Registrations are present with trading names whwn in $mode" in {

          val updatedAnswers: UserAnswers = baseAnswers
            .set(AllTradingNames, iossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value

          val application = applicationBuilder(
            userAnswers = Some(updatedAnswers),
            iossNumber = Some(iossNumber),
            iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
            numberOfIossRegistrations = 2
          )
            .build()

          running(application) {
            val request = FakeRequest(GET, addTradingNameRoute)

            val view = application.injector.instanceOf[AddTradingNameView]
            implicit val msgs: Messages = messages(application)
            val list = TradingNameSummary.addToListRows(updatedAnswers, mode)

            val result = route(application, request).value

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(
              form,
              mode,
              list,
              canAddTradingNames = true,
              Some(iossEtmpDisplayRegistration),
              2
            )(request, implicitly).toString
          }
        }
      }
    }
  }
}
