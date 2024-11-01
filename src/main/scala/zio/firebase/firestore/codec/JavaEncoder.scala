package zio.firebase.firestore.codec

import java.util.{Map as JMap, UUID}
import scala.compiletime.*
import scala.deriving.Mirror
import scala.jdk.CollectionConverters.*

enum Output:
  def value: Any
  case Leaf(value: Any)              extends Output
  case Node(value: Map[String, Any]) extends Output

trait JavaEncoder[A]:
  self =>

  def encode(value: A): Output

  final def contramap[B](f: B => A): JavaEncoder[B] =
    new JavaEncoder[B]:
      def encode(value: B): Output = self.encode(f(value))

object JavaEncoder extends EncoderLowPriority:
  import Output.*

  def apply[A](using encoder: JavaEncoder[A]): JavaEncoder[A] = encoder

  def apply[A](f: A => Map[String, Any]): JavaEncoder[A] = new JavaEncoder[A]:
    def encode(value: A): Output = Node(f(value))

  def valueEncoder[A]: JavaEncoder[A] = new JavaEncoder[A]:
    def encode(value: A): Output = Leaf(value)

  given byte: JavaEncoder[Byte]       = valueEncoder
  given short: JavaEncoder[Short]     = valueEncoder
  given int: JavaEncoder[Int]         = valueEncoder
  given long: JavaEncoder[Long]       = valueEncoder
  given float: JavaEncoder[Float]     = valueEncoder
  given double: JavaEncoder[Double]   = valueEncoder
  given char: JavaEncoder[Char]       = valueEncoder
  given boolean: JavaEncoder[Boolean] = valueEncoder
  given string: JavaEncoder[String]   = valueEncoder

  given uuid: JavaEncoder[UUID]             = string.contramap(_.toString)
  given bigDecimal: JavaEncoder[BigDecimal] = double.contramap(_.toDouble)

  given list[A](using encoder: JavaEncoder[A]): JavaEncoder[List[A]] with
    def encode(value: List[A]): Output =
      Leaf(value.map(v => toJMap(encoder.encode(v).value)).asJava)

  given map[A](using encoder: JavaEncoder[A]): JavaEncoder[Map[String, A]] with
    def encode(value: Map[String, A]): Output =
      Node(value.map((k, v) => k -> (toJMap(encoder.encode(v).value))).toMap)

  given option[A](using encoder: JavaEncoder[A]): JavaEncoder[Option[A]] with
    def encode(value: Option[A]): Output =
      Leaf(value.map(v => toJMap(encoder.encode(v).value)).orNull)

  given tuple2[A, B](using encA: JavaEncoder[A], encB: JavaEncoder[B]): JavaEncoder[(A, B)] with
    def encode(value: (A, B)): Output =
      Leaf((toJMap(encA.encode(value._1).value), toJMap(encB.encode(value._2).value)))

  private def toJMap(value: Any): Any = value match
    case nested: Map[_, _] => nested.asJava
    case other             => other

  inline def summonEncoder[A]: JavaEncoder[A] =
    summonFrom {
      case custom: JavaEncoder[A] => custom
      case _                      => summonInline[JavaEncoder[A]]
    }

  inline def deriveProduct[Names <: Tuple, Types <: Tuple](element: Product)(
    index: Int
  ): Map[String, Any] =
    inline erasedValue[(Names, Types)] match
      case (_: (name *: names), _: (tpe *: types)) =>
        val key     = constValue[name].toString
        val value   = element.productElement(index).asInstanceOf[tpe]
        val encoded = toJMap(summonEncoder[tpe].encode(value).value)
        deriveProduct[names, types](element)(index + 1) + (key -> encoded)
      case (_: EmptyTuple, _) =>
        Map.empty[String, Any]

  inline def deriveSum[Names <: Tuple, Types <: Tuple, A](value: A): Output =
    inline erasedValue[(Names, Types)] match
      case _: (EmptyTuple, EmptyTuple) =>
        throw new IllegalArgumentException(s"Cannot encode unknown coproduct value: $value")
      case _: ((name *: names), (tpe *: types)) =>
        val fieldName = constValue[name].toString

        inline summonInline[Mirror.Of[tpe]] match
          case m: Mirror.ProductOf[tpe] =>
            value match
              case v: tpe =>
                val encodedFields = summonEncoder[tpe].encode(v).value.asInstanceOf[Map[String, Any]]
                val result        = if encodedFields.isEmpty then fieldName else encodedFields
                Leaf(result)
              case _ =>
                deriveSum[names, types, A](value)
          case m: Mirror.SumOf[tpe] =>
            value match
              case v: tpe => Leaf(fieldName)
              case _      => deriveSum[names, types, A](value)

  inline given derived[A](using m: Mirror.Of[A]): JavaEncoder[A] = new JavaEncoder[A]:
    def encode(value: A): Output = inline m match
      case s: Mirror.SumOf[A] =>
        deriveSum[s.MirroredElemLabels, s.MirroredElemTypes, A](value)
      case p: Mirror.ProductOf[A] =>
        Node(
          deriveProduct[p.MirroredElemLabels, p.MirroredElemTypes](value.asInstanceOf[Product])(0)
        )

private[codec] trait EncoderLowPriority:
  this: JavaEncoder.type =>

  import java.time.*

  given instant: JavaEncoder[Instant]               = valueEncoder
  given localDate: JavaEncoder[LocalDate]           = valueEncoder
  given localTime: JavaEncoder[LocalTime]           = valueEncoder
  given offsetDateTime: JavaEncoder[OffsetDateTime] = valueEncoder
  given zonedDateTime: JavaEncoder[ZonedDateTime]   = valueEncoder
