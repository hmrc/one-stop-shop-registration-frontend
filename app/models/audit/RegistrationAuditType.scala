package models.audit

import models.{Enumerable, WithName}

sealed trait RegistrationAuditType {
  val auditType: String
  val transactionName: String
}

object RegistrationAuditType extends Enumerable.Implicits {
  case object CreateRegistration extends WithName("CreateRegistration") with RegistrationAuditType {
    override val auditType: String = "RegistrationSubmitted"
    override val transactionName: String = "registration-submitted"
  }
  case object AmendRegistration extends WithName("AmendRegistration") with RegistrationAuditType {
    override val auditType: String = "RegistrationAmended"
    override val transactionName: String = "registration-amended"
  }
}