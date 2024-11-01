package zio.firebase.firestore.codec

import zio.*
import zio.test.*

import java.time.*
import scala.jdk.CollectionConverters.*

import JavaEncoder.*

object JavaEncoderSpec extends ZIOSpecDefault:
  import examples.*
  val spec = suite("JavaEncoderSpec")(
    test("parameterless products") {
      assertTrue(Parameterless().toMap.asScala == Map())
    },
    test("products with primitives") {
      assertTrue(OnlyString("foo").toMap.asScala == Map("s" -> "foo")) &&
      assertTrue(Things(-1, 10.0f, false).toMap.asScala == Map("i" -> -1, "f" -> 10.0f, "b" -> false)) &&
      assertTrue(Things(0, -55, true).toMap.asScala == Map("i" -> 0, "f" -> -55, "b" -> true))
    },
    test("products with java.time types") {
      val instant   = Instant.parse("2021-01-01T00:00:00Z")
      val localDate = LocalDate.parse("2021-01-01")
      val localTime = LocalTime.parse("00:00:00")

      assertTrue(
        TimeStuff(instant, localDate, localTime).toMap.asScala == Map(
          "instant"   -> instant,
          "localDate" -> localDate,
          "localTime" -> localTime
        )
      )
    },
    test("nested products") {
      assertTrue(
        Nested("foo", Things(1, 2.0f, true)).toMap.asScala == Map(
          "s"      -> "foo",
          "things" -> Map("i" -> 1, "f" -> 2.0f, "b" -> true).asJava
        )
      )
    },
    test("products with options") {
      assertTrue(Optionals(Some("foo"), Some(1)).toMap.asScala == Map("s" -> "foo", "i" -> 1)) &&
      assertTrue(Optionals(None, Some(1)).toMap.asScala == Map("s" -> null, "i" -> 1)) &&
      assertTrue(Optionals(Some("foo"), None).toMap.asScala == Map("s" -> "foo", "i" -> null)) &&
      assertTrue(Optionals(None, None).toMap.asScala == Map("s" -> null, "i" -> null))
    },
    test("products with optional products") {
      assertTrue(OptionalProduct(Some(OnlyString("foo"))).toMap.asScala == Map("opt" -> Map("s" -> "foo").asJava)) &&
      assertTrue(OptionalProduct(None).toMap.asScala == Map("opt" -> null))
    },
    test("products with collections") {
      assertTrue(
        WithMap(Map("foo" -> 1, "bar" -> 2)).toMap.asScala == Map("m" -> Map("foo" -> 1, "bar" -> 2).asJava)
      ) &&
      assertTrue(WithList(List(1, 2, 3)).toMap.asScala == Map("l" -> List(1, 2, 3).asJava))
    },
    test("product with enum") {
      assertTrue(Colorful(Color.Red, Color.Green).toMap.asScala == Map("c1" -> "Red", "c2" -> "Green"))
    },
    test("optional enum") {
      assertTrue(OptionalEnum(Some(Color.Blue)).toMap.asScala == Map("opt" -> "Blue")) &&
      assertTrue(OptionalEnum(None).toMap.asScala == Map("opt" -> null))
    },
    test("sealed trait") {
      assertTrue(
        SealedExample(Child1("foo"), Child2(42)).toMap.asScala == Map(
          "a" -> Map("name" -> "foo").asJava,
          "b" -> Map("age" -> 42).asJava
        )
      )
    },
    test("case class with newtype") {
      assertTrue(User(Email.wrap("email@valid.com")).toMap.asScala == Map("email" -> "email@valid.com"))
    }
  )
