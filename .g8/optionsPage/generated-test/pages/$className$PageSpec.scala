package pages

import base.SpecBase
import models.$className$
import pages.behaviours.PageBehaviours

class $className$Spec extends SpecBase with PageBehaviours {

  "$className$Page" - {

    beRetrievable[$className$]($className$Page)

    beSettable[$className$]($className$Page)

    beRemovable[$className$]($className$Page)
  }
}
