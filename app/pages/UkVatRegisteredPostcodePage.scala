package pages

import play.api.libs.json.JsPath

case object UkVatRegisteredPostcodePage extends QuestionPage[String] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "ukVatRegisteredPostcode"
}
