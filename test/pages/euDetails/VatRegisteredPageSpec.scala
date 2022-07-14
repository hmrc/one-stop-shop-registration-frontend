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

package pages.euDetails

import base.SpecBase
import controllers.euDetails.{routes => euRoutes}
import models.{CheckLoopMode, CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class VatRegisteredPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "VatRegisteredInEuPage" - {

    beRetrievable[Boolean](VatRegisteredPage(index))

    beSettable[Boolean](VatRegisteredPage(index))

    beRemovable[Boolean](VatRegisteredPage(index))

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "to EU VAT Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

          VatRegisteredPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuVatNumberController.onPageLoad(NormalMode, index))
        }
      }

      "when the answer is no" - {

        "to Has Fixed Establishment for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index))
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          VatRegisteredPage(index).navigate(NormalMode, emptyUserAnswers)
            .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "and EU VAT number has not been answered" - {

          "to EU VAT number" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

            VatRegisteredPage(index).navigate(CheckMode, answers)
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(CheckMode, index))
          }
        }

        "and EU VAT number has been answered" - {

          "to wherever EU VAT number would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(EuVatNumberPage(index), "foo").success.value

            VatRegisteredPage(index).navigate(CheckMode, answers)
              .mustEqual(EuVatNumberPage(index).navigate(CheckMode, answers))
          }
        }
      }

      "when the answer is no" - {

        "and Has Fixed Establishment has not been answered" - {

          "to Has Fixed Establishment" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

            VatRegisteredPage(index).navigate(CheckMode, answers)
              .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(CheckMode, index))
          }
        }

        "and Has Fixed Establishment has been answered" - {

          "to wherever Has Fixed Establishment navigates to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), false).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value

            VatRegisteredPage(index).navigate(CheckMode, answers)
              .mustEqual(HasFixedEstablishmentPage(index).navigate(CheckMode, answers))
          }
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          VatRegisteredPage(index).navigate(CheckMode, emptyUserAnswers)
            .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when the answer is yes" - {

        "and EU VAT number has not been answered" - {

          "to EU VAT number" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

            VatRegisteredPage(index).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(CheckLoopMode, index))
          }
        }

        "and EU VAT number has been answered" - {

          "to wherever EU VAT number would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(EuVatNumberPage(index), "foo").success.value

            VatRegisteredPage(index).navigate(CheckLoopMode, answers)
              .mustEqual(EuVatNumberPage(index).navigate(CheckLoopMode, answers))
          }
        }
      }

      "when the answer is no" - {

        "and Has Fixed Establishment has not been answered" - {

          "to Has Fixed Establishment" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

            VatRegisteredPage(index).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(CheckLoopMode, index))
          }
        }

        "and Has Fixed Establishment has been answered" - {

          "to wherever Has Fixed Establishment navigates to" in {

            val hasFixedEstablishmentAnswer = arbitrary[Boolean].sample.value
            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), false).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value

            VatRegisteredPage(index).navigate(CheckLoopMode, answers)
              .mustEqual(HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers))
          }
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          VatRegisteredPage(index).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must delete EU VAT number when the answer is no" in {

      val baseAnswers =
        emptyUserAnswers
          .set(EuVatNumberPage(index), "123").success.value
          .set(EuTaxReferencePage(index), "456").success.value

      val result = baseAnswers.set(VatRegisteredPage(index), false).success.value

      result.get(EuVatNumberPage(index)) must not be defined
      result.get(EuTaxReferencePage(index)).value mustEqual "456"
    }

    "must delete EU Tax Reference number when the answer is yes" in {

      val baseAnswers =
        emptyUserAnswers
          .set(EuVatNumberPage(index), "123").success.value
          .set(EuTaxReferencePage(index), "456").success.value

      val result = baseAnswers.set(VatRegisteredPage(index), true).success.value

      result.get(EuVatNumberPage(index)).value mustEqual "123"
      result.get(EuTaxReferencePage(index)) must not be defined
    }
  }
}
