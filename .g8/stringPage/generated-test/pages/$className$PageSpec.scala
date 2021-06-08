package pages

import base.SpecBase
import pages.behaviours.PageBehaviours

class $className$PageSpec extends SpecBase with PageBehaviours {

  "$className$Page" - {

    beRetrievable[String]($className$Page)

    beSettable[String]($className$Page)

    beRemovable[String]($className$Page)
  }
}
