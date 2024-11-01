package zio.firebase.firestore.codec

import java.time.*
import zio.prelude.Subtype
import zio.prelude.Assertion.matches

object examples:
  case class Parameterless() derives JavaEncoder, JavaDecoder
  case class OnlyString(s: String) derives JavaEncoder, JavaDecoder
  case class Things(i: Int, f: Float, b: Boolean) derives JavaEncoder, JavaDecoder
  case class TimeStuff(instant: Instant, localDate: LocalDate, localTime: LocalTime) derives JavaEncoder, JavaDecoder
  case class Nested(s: String, things: Things) derives JavaEncoder, JavaDecoder
  case class Optionals(s: Option[String], i: Option[Int]) derives JavaEncoder, JavaDecoder
  case class OptionalProduct(opt: Option[OnlyString]) derives JavaEncoder, JavaDecoder
  case class WithMap(m: Map[String, Int]) derives JavaEncoder, JavaDecoder
  case class WithList(l: List[Int]) derives JavaEncoder, JavaDecoder

  enum Color derives JavaEncoder, JavaDecoder:
    case Red, Green, Blue
  case class Colorful(c1: Color, c2: Color) derives JavaEncoder, JavaDecoder

  case class OptionalEnum(opt: Option[Color]) derives JavaEncoder, JavaDecoder

  sealed abstract class Parent
  case class Child1(name: String) extends Parent
  case class Child2(age: Int)     extends Parent

  case class SealedExample(a: Parent, b: Parent) derives JavaEncoder, JavaDecoder

  type Email = Email.Type
  object Email extends Subtype[String]:
    private val emailRegex =
      "^(?=.{1,64}@)[A-Za-z0-9_-]+((\\.|\\+)[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    override inline def assertion = matches(emailRegex)

    given [T](using JavaEncoder[T]): JavaEncoder[Type] = derive[JavaEncoder]
    given [T](using JavaDecoder[T]): JavaDecoder[Type] = derive[JavaDecoder]

  case class User(email: Email) derives JavaEncoder, JavaDecoder
