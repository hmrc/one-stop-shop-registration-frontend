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

package pages

import base.SpecBase
import controllers.routes
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.behaviours.PageBehaviours
import services.FeatureFlagService

class InControlOfMovingGoodsPageSpec extends SpecBase with PageBehaviours with MockitoSugar with BeforeAndAfterEach {

  private val features = mock[FeatureFlagService]

  private val page = new InControlOfMovingGoodsPage(features)

  override def beforeEach(): Unit = {
    Mockito.reset(features)
  }

  "InControlOfMovingGoodsPage" - {

    "when the answer is yes" - {

      "and the scheme has started" - {

        "must navigate to HasMadeSales" in {

          when(features.schemeHasStarted) thenReturn true

          page.navigate(true) mustEqual routes.HasMadeSalesController.onPageLoad()
        }
      }

      "and the scheme has not started yet" - {

        "must navigate to auth.onSignIn" in {

          when(features.schemeHasStarted) thenReturn false

          page.navigate(true) mustEqual controllers.auth.routes.AuthController.onSignIn()
        }
      }
    }

    "when the answer is no" - {

      "must navigate to Not In Control of Moving Goods" in {

        page.navigate(false) mustEqual routes.NotInControlOfMovingGoodsController.onPageLoad()
      }
    }
  }
}
