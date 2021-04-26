package pages

import java.time.LocalDate

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class UkVatEffectiveDatePageSpec extends PageBehaviours {

  "UkVatEffectiveDatePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](UkVatEffectiveDatePage)

    beSettable[LocalDate](UkVatEffectiveDatePage)

    beRemovable[LocalDate](UkVatEffectiveDatePage)
  }
}
