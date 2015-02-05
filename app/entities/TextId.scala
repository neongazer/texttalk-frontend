package entities

import scalaz.Equal


object TextId{
  //this typeclass allows use of scalaz's type-safe === method
  implicit val equals: Equal[TextId] = Equal.equal(_ == _)
}


//Post Id wrapper class.
case class TextId(tid: String) extends AnyVal