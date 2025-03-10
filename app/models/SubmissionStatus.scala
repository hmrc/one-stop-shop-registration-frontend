package models

sealed trait SubmissionStatus

object SubmissionStatus extends Enumerable.Implicits {

  case object Due extends WithName("DUE") with SubmissionStatus

  case object Overdue extends WithName("OVERDUE") with SubmissionStatus

  case object Complete extends WithName("COMPLETE") with SubmissionStatus

  case object Next extends WithName("NEXT") with SubmissionStatus

  case object Excluded extends WithName("EXCLUDED") with SubmissionStatus

  case object Expired extends WithName("EXPIRED") with SubmissionStatus

  val values: Seq[SubmissionStatus] = Seq(Due, Overdue, Complete, Next, Excluded, Expired)

  implicit val enumerable: Enumerable[SubmissionStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
