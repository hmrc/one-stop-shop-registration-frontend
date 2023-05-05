package pages.euDetails

import base.SpecBase
import pages.behaviours.PageBehaviours

class DeleteAllEuDetailsPageSpec extends SpecBase with PageBehaviours {

  "DeleteAllEuDetailsPage" - {

    beRetrievable[Boolean](DeleteAllEuDetailsPage)

    beSettable[Boolean](DeleteAllEuDetailsPage)

    beRemovable[Boolean](DeleteAllEuDetailsPage)
  }
}
