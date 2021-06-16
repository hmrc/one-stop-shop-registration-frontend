package pages

import base.SpecBase
import pages.behaviours.PageBehaviours

class $className$PageSpec extends SpecBase with PageBehaviours {

  "$className$Page" - {

    beRetrievable[Int]($className$Page)

    beSettable[Int]($className$Page)

    beRemovable[Int]($className$Page)
  }
}
