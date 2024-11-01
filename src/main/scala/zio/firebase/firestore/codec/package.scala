package zio.firebase.firestore

import java.util.{List as JList, Map as JMap}
import scala.jdk.CollectionConverters.*

package object codec:
  implicit final class DecoderOps(private val jmap: JMap[String, Any]) extends AnyVal:
    def fromJMap[T](implicit decoder: JavaDecoder[T]): Either[String, T] = decoder.decode(jmap)

  import Output.*
  extension [T](value: T)(using encoder: JavaEncoder[T])
    def toMap: JMap[String, Any] =
      encoder.encode(value) match
        case Node(map)                          => map.asJava
        case Leaf(v) if v.isInstanceOf[Product] => Map.empty[String, Any].asJava
        case _                                  => throw new IllegalArgumentException("Not a product type")
