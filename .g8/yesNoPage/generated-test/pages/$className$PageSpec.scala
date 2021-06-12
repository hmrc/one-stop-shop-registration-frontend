package pages

import base.SpecBase
import pages.behaviours.PageBehaviours

class $className$PageSpec SpecBase with extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[Boolean]($className$Page)

    beSettable[Boolean]($className$Page)

    beRemovable[Boolean]($className$Page)
  }
}
