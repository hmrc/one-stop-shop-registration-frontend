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

package pages.euDetails

import base.SpecBase
import controllers.euDetails.routes as euRoutes
import controllers.amend.routes as amendRoutes
import controllers.rejoin.routes as rejoinRoutes
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, NormalMode, RejoinLoopMode, RejoinMode}
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

        "to Cannot Add Country Without Vat Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(NormalMode, index))
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

        "to Cannot Add Country Without Vat Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(CheckMode, answers)
            .mustEqual(euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(CheckMode, index))
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

        "to Cannot Add Country Without Vat Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(CheckLoopMode, answers)
            .mustEqual(euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(CheckLoopMode, index))
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          VatRegisteredPage(index).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Amend mode" - {

      "when the answer is yes" - {

        "and EU VAT number has not been answered" - {

          "to EU VAT number" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

            VatRegisteredPage(index).navigate(AmendMode, answers)
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(AmendMode, index))
          }
        }

        "and EU VAT number has been answered" - {

          "to wherever EU VAT number would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(EuVatNumberPage(index), "foo").success.value

            VatRegisteredPage(index).navigate(AmendMode, answers)
              .mustEqual(EuVatNumberPage(index).navigate(AmendMode, answers))
          }
        }
      }

      "when the answer is no" - {

        "to Cannot Add Country Without Vat Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(AmendMode, answers)
            .mustEqual(euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(AmendMode, index))
        }
      }

      "when the answer is empty" - {

        "to Amend Journey recovery" in {

          VatRegisteredPage(index).navigate(AmendMode, emptyUserAnswers)
            .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Amend Loop mode" - {

      "when the answer is yes" - {

        "and EU VAT number has not been answered" - {

          "to EU VAT number" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

            VatRegisteredPage(index).navigate(AmendLoopMode, answers)
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(AmendLoopMode, index))
          }
        }

        "and EU VAT number has been answered" - {

          "to wherever EU VAT number would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(EuVatNumberPage(index), "foo").success.value

            VatRegisteredPage(index).navigate(AmendLoopMode, answers)
              .mustEqual(EuVatNumberPage(index).navigate(AmendLoopMode, answers))
          }
        }
      }

      "when the answer is no" - {

        "to Cannot Add Country Without Vat Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(AmendLoopMode, answers)
            .mustEqual(euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(AmendLoopMode, index))
        }
      }

      "when the answer is empty" - {

        "to Amend Journey recovery" in {

          VatRegisteredPage(index).navigate(AmendLoopMode, emptyUserAnswers)
            .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Rejoin mode" - {

      "when the answer is yes" - {

        "and EU VAT number has not been answered" - {

          "to EU VAT number" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

            VatRegisteredPage(index).navigate(RejoinMode, answers)
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(RejoinMode, index))
          }
        }

        "and EU VAT number has been answered" - {

          "to wherever EU VAT number would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(EuVatNumberPage(index), "foo").success.value

            VatRegisteredPage(index).navigate(RejoinMode, answers)
              .mustEqual(EuVatNumberPage(index).navigate(RejoinMode, answers))
          }
        }
      }

      "when the answer is no" - {

        "to Cannot Add Country Without Vat Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(RejoinMode, answers)
            .mustEqual(euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(RejoinMode, index))
        }
      }

      "when the answer is empty" - {

        "to Rejoin Journey recovery" in {

          VatRegisteredPage(index).navigate(RejoinMode, emptyUserAnswers)
            .mustEqual(rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Rejoin Loop mode" - {

      "when the answer is yes" - {

        "and EU VAT number has not been answered" - {

          "to EU VAT number" in {

            val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

            VatRegisteredPage(index).navigate(RejoinLoopMode, answers)
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(RejoinLoopMode, index))
          }
        }

        "and EU VAT number has been answered" - {

          "to wherever EU VAT number would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(EuVatNumberPage(index), "foo").success.value

            VatRegisteredPage(index).navigate(AmendLoopMode, answers)
              .mustEqual(EuVatNumberPage(index).navigate(AmendLoopMode, answers))
          }
        }
      }

      "when the answer is no" - {

        "to Cannot Add Country Without Vat Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(AmendLoopMode, answers)
            .mustEqual(euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(AmendLoopMode, index))
        }
      }

      "when the answer is empty" - {

        "to Amend Journey recovery" in {

          VatRegisteredPage(index).navigate(AmendLoopMode, emptyUserAnswers)
            .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
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
