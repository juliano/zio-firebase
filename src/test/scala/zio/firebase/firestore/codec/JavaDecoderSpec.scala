package zio.firebase.firestore.codec

import zio.*
import zio.test.*

import java.time.*
import java.util.{HashMap, Map as JMap}
import scala.jdk.CollectionConverters.*

import JavaDecoder.*

object JavaDecoderSpec extends ZIOSpecDefault:
  import examples.*

  val spec = suite("JavaDecoderSpec")(
    test("parameterless products") {
      val map = Map[String, Any]().asJava
      assertTrue(map.fromJMap[Parameterless] == Right(Parameterless()))
    },
    test("products with primitives") {
      val map1 = JMap.of[String, Any]("s", "foo")
      val map2 = JMap.of[String, Any]("i", -1, "f", 10.0f, "b", false)
      val map3 = JMap.of[String, Any]("i", 0, "f", -55f, "b", true)
      assertTrue(map1.fromJMap[OnlyString] == Right(OnlyString("foo"))) &&
      assertTrue(map2.fromJMap[Things] == Right(Things(-1, 10.0f, false))) &&
      assertTrue(map3.fromJMap[Things] == Right(Things(0, -55f, true)))
    },
    test("products with java.time types") {
      val instant   = Instant.parse("2021-01-01T00:00:00Z")
      val localDate = LocalDate.parse("2021-01-01")
      val localTime = LocalTime.parse("00:00:00")
      val map = JMap.of[String, Any](
        "instant",
        instant,
        "localDate",
        localDate,
        "localTime",
        localTime
      )
      assertTrue(map.fromJMap[TimeStuff] == Right(TimeStuff(instant, localDate, localTime)))
    },
    test("nested products") {
      val map = JMap.of[String, Any](
        "s",
        "foo",
        "things",
        JMap.of[String, Any]("i", 1, "f", 2.0f, "b", true)
      )
      assertTrue(map.fromJMap[Nested] == Right(Nested("foo", Things(1, 2.0f, true))))
    },
    test("products with options") {
      val map1 = JMap.of[String, Any]("s", "foo", "i", 1)
      val map2 = JMap.of[String, Any]("i", 1)
      val map3 = JMap.of[String, Any]("s", "foo")
      val map4 = JMap.of[String, Any]()
      assertTrue(map1.fromJMap[Optionals] == Right(Optionals(Some("foo"), Some(1)))) &&
      assertTrue(map2.fromJMap[Optionals] == Right(Optionals(None, Some(1)))) &&
      assertTrue(map3.fromJMap[Optionals] == Right(Optionals(Some("foo"), None))) &&
      assertTrue(map4.fromJMap[Optionals] == Right(Optionals(None, None)))
    },
    test("products with optional products") {
      val map1 = JMap.of[String, Any]("opt", JMap.of[String, Any]("s", "foo"))
      val map2 = new HashMap[String, Any]()
      map2.put("s", null)
      assertTrue(map1.fromJMap[OptionalProduct] == Right(OptionalProduct(Some(OnlyString("foo"))))) &&
      assertTrue(map2.fromJMap[OptionalProduct] == Right(OptionalProduct(None)))
    },
    test("products with collections") {
      val map1 = JMap.of[String, Any]("m", JMap.of[String, Any]("foo", 1, "bar", 2))
      val map2 = JMap.of[String, Any]("l", List(1, 2, 3).asJava)
      assertTrue(map1.fromJMap[WithMap] == Right(WithMap(Map("foo" -> 1, "bar" -> 2)))) &&
      assertTrue(map2.fromJMap[WithList] == Right(WithList(List(1, 2, 3))))
    },
    test("product with enum") {
      val map = JMap.of[String, Any]("c1", "Red", "c2", "Green")
      assertTrue(map.fromJMap[Colorful] == Right(Colorful(Color.Red, Color.Green)))
    },
    test("optional enum") {
      val map1 = JMap.of[String, Any]("opt", "Blue")
      val map2 = JMap.of[String, Any]()
      assertTrue(map1.fromJMap[OptionalEnum] == Right(OptionalEnum(Some(Color.Blue)))) &&
      assertTrue(map2.fromJMap[OptionalEnum] == Right(OptionalEnum(None)))
    },
    test("sealed trait") {
      val map = JMap.of[String, Any]("a", JMap.of[String, Any]("name", "foo"), "b", JMap.of[String, Any]("age", 42))
      assertTrue(map.fromJMap[SealedExample] == Right(SealedExample(Child1("foo"), Child2(42))))
    }
  )
