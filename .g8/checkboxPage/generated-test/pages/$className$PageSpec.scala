package pages

import base.SpecBase
import models.$className$
import pages.behaviours.PageBehaviours

class $className$PageSpec extends SpecBase with PageBehaviours {

  "$className$Page" - {

    beRetrievable[Set[$className$]]($className$Page)

    beSettable[Set[$className$]]($className$Page)

    beRemovable[Set[$className$]]($className$Page)
  }
}
