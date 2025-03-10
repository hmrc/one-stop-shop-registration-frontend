package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Return (
                    firstDay: LocalDate,
                    lastDay: LocalDate,
                    dueDate: LocalDate,
                    submissionStatus: SubmissionStatus,
                    inProgress: Boolean,
                    isOldest: Boolean
                  )

case object Return {

  implicit val format: OFormat[Return] = Json.format[Return]
}
