package models

import play.api.libs.json.{Json, OFormat}

case class CurrentReturns(
                           returns: Seq[Return],
                           excluded: Boolean = false,
                           finalReturnsCompleted: Boolean,
                           completeOrExcludedReturns: Seq[Return] = Seq.empty
                         )

case object CurrentReturns {
  implicit val format: OFormat[CurrentReturns] = Json.format[CurrentReturns]
}
