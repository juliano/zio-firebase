package zio.firebase.firestore.codec

import scala.deriving.Mirror
import scala.jdk.CollectionConverters.*

import java.util.{Map as JMap, UUID}

final case class JavaCodec[A](encoder: JavaEncoder[A], decoder: JavaDecoder[A]):
  final def to(value: A): JMap[String, Any] = encoder.encode(value) match
    case Output.Node(map) => map.asJava
    case _                => throw new IllegalArgumentException("Not a product type")

  final def from(map: JMap[String, Any]): Either[String, A] = decoder.decode(map)

  final def transform[B](f: A => B, g: B => A): JavaCodec[B] =
    JavaCodec(encoder.contramap(g), decoder.map(f))

  final def transformOrFail[B](f: A => Either[String, B], g: B => A): JavaCodec[B] =
    JavaCodec(encoder.contramap(g), decoder.mapOrFail(f))

object JavaCodec extends CodecLowPriority:
  def apply[A](using codec: JavaCodec[A]): JavaCodec[A] = codec

  inline def derived[A](using mirror: Mirror.Of[A]): JavaCodec[A] =
    val encoder = JavaEncoder.derived[A]
    val decoder = JavaDecoder.derived[A]
    JavaCodec(encoder, decoder)

  given byte: JavaCodec[Byte]       = JavaCodec(JavaEncoder.byte, JavaDecoder.byte)
  given short: JavaCodec[Short]     = JavaCodec(JavaEncoder.short, JavaDecoder.short)
  given int: JavaCodec[Int]         = JavaCodec(JavaEncoder.int, JavaDecoder.int)
  given long: JavaCodec[Long]       = JavaCodec(JavaEncoder.long, JavaDecoder.long)
  given float: JavaCodec[Float]     = JavaCodec(JavaEncoder.float, JavaDecoder.float)
  given double: JavaCodec[Double]   = JavaCodec(JavaEncoder.double, JavaDecoder.double)
  given char: JavaCodec[Char]       = JavaCodec(JavaEncoder.char, JavaDecoder.char)
  given boolean: JavaCodec[Boolean] = JavaCodec(JavaEncoder.boolean, JavaDecoder.boolean)
  given string: JavaCodec[String]   = JavaCodec(JavaEncoder.string, JavaDecoder.string)

  given uuid: JavaCodec[UUID]             = JavaCodec(JavaEncoder.uuid, JavaDecoder.uuid)
  given bigDecimal: JavaCodec[BigDecimal] = JavaCodec(JavaEncoder.bigDecimal, JavaDecoder.bigDecimal)

  given option[A: JavaEncoder: JavaDecoder]: JavaCodec[Option[A]] =
    JavaCodec(JavaEncoder.option[A], JavaDecoder.option[A])

  given list[A: JavaEncoder: JavaDecoder]: JavaCodec[List[A]] =
    JavaCodec(JavaEncoder.list[A], JavaDecoder.list[A])

  given map[A: JavaEncoder: JavaDecoder]: JavaCodec[Map[String, A]] =
    JavaCodec(JavaEncoder.map[A], JavaDecoder.map[A])

  given tuple2[A: JavaEncoder: JavaDecoder, B: JavaEncoder: JavaDecoder]: JavaCodec[(A, B)] =
    JavaCodec(JavaEncoder.tuple2[A, B], JavaDecoder.tuple2[A, B])

private[codec] trait CodecLowPriority:
  this: JavaCodec.type =>

  import java.time.*

  given instant: JavaCodec[Instant]               = JavaCodec(JavaEncoder.instant, JavaDecoder.instant)
  given localDate: JavaCodec[LocalDate]           = JavaCodec(JavaEncoder.localDate, JavaDecoder.localDate)
  given localTime: JavaCodec[LocalTime]           = JavaCodec(JavaEncoder.localTime, JavaDecoder.localTime)
  given offsetDateTime: JavaCodec[OffsetDateTime] = JavaCodec(JavaEncoder.offsetDateTime, JavaDecoder.offsetDateTime)
  given zonedDateTime: JavaCodec[ZonedDateTime]   = JavaCodec(JavaEncoder.zonedDateTime, JavaDecoder.zonedDateTime)
