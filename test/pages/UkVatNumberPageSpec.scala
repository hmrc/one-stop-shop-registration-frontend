package pages

import pages.behaviours.PageBehaviours


class UkVatNumberPageSpec extends PageBehaviours {

  "UkVatNumberPage" - {

    beRetrievable[String](UkVatNumberPage)

    beSettable[String](UkVatNumberPage)

    beRemovable[String](UkVatNumberPage)
  }
}
