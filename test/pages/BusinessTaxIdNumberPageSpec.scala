package pages

import base.SpecBase
import pages.behaviours.PageBehaviours

class BusinessTaxIdNumberPageSpec extends SpecBase with PageBehaviours {

  "BusinessTaxIdNumberPage" - {

    beRetrievable[String](BusinessTaxIdNumberPage)

    beSettable[String](BusinessTaxIdNumberPage)

    beRemovable[String](BusinessTaxIdNumberPage)
  }
}
