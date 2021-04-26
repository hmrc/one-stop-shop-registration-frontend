package pages

import pages.behaviours.PageBehaviours


class UkVatRegisteredPostcodePageSpec extends PageBehaviours {

  "UkVatRegisteredPostcodePage" - {

    beRetrievable[String](UkVatRegisteredPostcodePage)

    beSettable[String](UkVatRegisteredPostcodePage)

    beRemovable[String](UkVatRegisteredPostcodePage)
  }
}
