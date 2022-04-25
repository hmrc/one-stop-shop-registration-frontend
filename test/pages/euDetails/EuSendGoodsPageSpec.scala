package pages.euDetails

import base.SpecBase
import pages.EuSendGoodsPage
import pages.behaviours.PageBehaviours

class EuSendGoodsPageSpec extends SpecBase with PageBehaviours {

  "EuSendGoodsPage" - {

    beRetrievable[Boolean](EuSendGoodsPage)

    beSettable[Boolean](EuSendGoodsPage)

    beRemovable[Boolean](EuSendGoodsPage)
  }
}
