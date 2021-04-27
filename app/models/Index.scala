package models

import play.api.mvc.PathBindable

case class Index(position: Int) {
  val display: Int = position + 1
}

object Index {

  implicit def indexPathBindable(implicit intBinder: PathBindable[Int]): PathBindable[Index] = new PathBindable[Index] {

    override def bind(key: String, value: String): Either[String, Index] =
      intBinder.bind(key, value) match {
        case Right(x) if x > 0 => Right(Index(x - 1))
        case _                 => Left("Index binding failed")
      }

    override def unbind(key: String, value: Index): String =
      intBinder.unbind(key, value.position + 1)
  }
}