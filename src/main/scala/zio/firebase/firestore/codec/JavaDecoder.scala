package zio.firebase.firestore.codec

import java.util.{List as JList, Map as JMap, UUID}
import scala.compiletime.*
import scala.deriving.Mirror
import scala.jdk.CollectionConverters.*
import org.checkerframework.checker.units.qual.s

trait JavaDecoder[A]:
  self =>

  def decode(value: JMap[String, Any]): Either[String, A]

  final def map[B](f: A => B): JavaDecoder[B] =
    new JavaDecoder[B]:
      def decode(value: JMap[String, Any]): Either[String, B] =
        self.decode(value).map(f)

  final def mapOrFail[B](f: A => Either[String, B]): JavaDecoder[B] =
    new JavaDecoder[B]:
      def decode(value: JMap[String, Any]): Either[String, B] =
        self.decode(value).flatMap(f)

object JavaDecoder extends DecoderLowPriority:
  def apply[A](using decoder: JavaDecoder[A]): JavaDecoder[A] = decoder

  inline def valueDecoder[A]: JavaDecoder[A] = new JavaDecoder[A]:
    def decode(value: JMap[String, Any]): Either[String, A] =
      Option(value.get("value"))
        .map(v => Right(v.asInstanceOf[A]))
        .getOrElse(Left("Missing value for primitive:" + value))

  given byte: JavaDecoder[Byte]       = valueDecoder
  given short: JavaDecoder[Short]     = valueDecoder
  given int: JavaDecoder[Int]         = valueDecoder
  given long: JavaDecoder[Long]       = valueDecoder
  given float: JavaDecoder[Float]     = valueDecoder
  given double: JavaDecoder[Double]   = valueDecoder
  given char: JavaDecoder[Char]       = valueDecoder
  given boolean: JavaDecoder[Boolean] = valueDecoder
  given string: JavaDecoder[String]   = valueDecoder

  given uuid: JavaDecoder[UUID]             = string.map(UUID.fromString)
  given bigDecimal: JavaDecoder[BigDecimal] = double.map(BigDecimal.apply)

//   given [T](using decoder: JavaDecoder[T]): JavaDecoder[List[T]] with
//     def decode(value: JMap[String, Object]): List[T] =
//       val list = value.get("value").asInstanceOf[JList[Object]].asScala
//       list.map(obj => decoder.decode(Map("value" -> obj).asJava)).toList

  given list[A](using decoder: JavaDecoder[A]): JavaDecoder[List[A]] with
    def decode(value: JMap[String, Any]): Either[String, List[A]] =
      Option(value.get("value")) match
        case Some(v) =>
          val list = v.asInstanceOf[JList[Object]].asScala
          list.foldRight(Right(Nil): Either[String, List[A]]) { (elem, acc) =>
            for
              decoded <- decoder.decode(Map("value" -> elem).asJava)
              rest    <- acc
            yield decoded :: rest
          }
        case _ => Right(Nil)

  given map[A](using decoder: JavaDecoder[A]): JavaDecoder[Map[String, A]] with
    def decode(value: JMap[String, Any]): Either[String, Map[String, A]] =
      Right(
        value.asScala.toMap.collect { case (k, v) =>
          decoder.decode(Map("value" -> v).asJava).map(k -> _)
        }.collect { case Right(result) => result }.toMap
      )

  given option[A](using decoder: JavaDecoder[A]): JavaDecoder[Option[A]] with
    def decode(value: JMap[String, Any]): Either[String, Option[A]] =
      if value.isEmpty then Right(None)
      else decoder.decode(value).map(Some(_))

  given tuple2[A, B](using decA: JavaDecoder[A], decB: JavaDecoder[B]): JavaDecoder[(A, B)] with
    def decode(value: JMap[String, Any]): Either[String, (A, B)] =
      for
        a <- decA.decode(value)
        b <- decB.decode(value)
      yield (a, b)

  inline def summonDecoder[A]: JavaDecoder[A] =
    summonFrom {
      case custom: JavaDecoder[A] => custom
      case _                      => summonInline[JavaDecoder[A]]
    }

  inline def deriveProduct[Names <: Tuple, Types <: Tuple, A](
    map: JMap[String, Any],
    inline construct: Seq[Any] => A,
    index: Int
  ): Either[String, A] =
    inline erasedValue[(Names, Types)] match
      case (_: (name *: names), _: (tpe *: types)) =>
        val fieldName = constValue[name].toString
        val fieldValue = Option(map.get(fieldName)) match
          case Some(m: JMap[?, ?]) => summonDecoder[tpe].decode(m.asInstanceOf[JMap[String, Any]])
          case Some(v)             => summonDecoder[tpe].decode(Map("value" -> v).asJava)
          case None                => handleNull[tpe]
        fieldValue.flatMap(fv => deriveProduct[names, types, A](map, args => construct(fv +: args), index + 1))
      case (_: EmptyTuple, _) =>
        Right(construct(Seq.empty))

  inline def handleNull[A]: Either[String, Any] =
    inline erasedValue[A] match
      case _: Option[?] => Right(None)
      case _: List[?]   => Right(Nil)
      case _: Seq[?]    => Right(Nil)
      case _: Map[?, ?] => Right(Map.empty)
      case _            => Left("Missing value for non-optional field")

  inline def deriveSum[Names <: Tuple, Types <: Tuple, A](
    map: JMap[String, Any]
  ): Either[String, A] =
    inline erasedValue[(Names, Types)] match
      case (_: (name *: names), _: (tpe *: types)) =>
        def decodeField[A](fieldName: String, map: JMap[String, Any]): Either[String, A] =
          val fieldValue = map.asScala.values.find(_.toString == fieldName) match
            case Some(value) => summonDecoder[tpe].decode(Map("value" -> value).asJava)
            case None        => deriveSum[names, types, A](map)
          fieldValue.flatMap(fv => Right(fv.asInstanceOf[A]))

        val fieldName = constValue[name].toString
        inline summonInline[Mirror.Of[tpe]] match
          case m: Mirror.ProductOf[tpe] =>
            val fieldNames = constValueTuple[m.MirroredElemLabels].toList.asInstanceOf[List[String]]
            if (fieldNames.exists(map.containsKey)) then summonDecoder[tpe].decode(map).map(_.asInstanceOf[A])
            else decodeField(fieldName, map)

          case _: Mirror.SumOf[tpe] => decodeField(fieldName, map)
      case _: (EmptyTuple, EmptyTuple) =>
        throw new IllegalArgumentException(s"Invalid coproduct type")

  inline given derived[A](using m: Mirror.Of[A]): JavaDecoder[A] = new JavaDecoder[A]:
    def decode(value: JMap[String, Any]): Either[String, A] =
      inline m match
        case p: Mirror.ProductOf[A] =>
          deriveProduct[p.MirroredElemLabels, p.MirroredElemTypes, A](
            value,
            args => p.fromProduct(Tuple.fromArray(args.toArray)),
            0
          )
        case s: Mirror.SumOf[A] =>
          deriveSum[s.MirroredElemLabels, s.MirroredElemTypes, A](value)

private[codec] trait DecoderLowPriority:
  this: JavaDecoder.type =>

  import java.time.*

  given instant: JavaDecoder[Instant]               = valueDecoder
  given localDate: JavaDecoder[LocalDate]           = valueDecoder
  given localTime: JavaDecoder[LocalTime]           = valueDecoder
  given offsetDateTime: JavaDecoder[OffsetDateTime] = valueDecoder
  given zonedDateTime: JavaDecoder[ZonedDateTime]   = valueDecoder
