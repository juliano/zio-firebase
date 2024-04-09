package zio.firebase

import zio.IO
import zio.prelude.Assertion.*
import zio.prelude.{Subtype, Validation}

package object messaging:
  type DeviceToken = DeviceToken.Type
  object DeviceToken extends Subtype[String]:
    override inline def assertion = hasLength(greaterThan(0))

    private val message = s"Failure making DeviceToken: Option is empty"
    def make(opt: Option[String]): Validation[String, DeviceToken] =
      Validation.fromOption(opt).flatMap(make(_)).mapError(_ => message)

    def makeZIO(s: String): IO[IllegalArgumentException, DeviceToken] =
      make(s).toZIO.mapError(new IllegalArgumentException(_))

    def makeZIO(opt: Option[String]): IO[IllegalArgumentException, DeviceToken] =
      make(opt).toZIO.mapError(new IllegalArgumentException(_))

  type Title = Title.Type
  object Title extends Subtype[String]

  type Body = Body.Type
  object Body extends Subtype[String]

  type Color = Color.Type
  object Color extends Subtype[String]:
    private final val regex       = "^#((?i)[a-f0-9]{6})\\b$"
    override inline def assertion = matches(regex)

  type Icon = Icon.Type
  object Icon extends Subtype[String]

  type Image = Image.Type
  object Image extends Subtype[String]

  type AnalyticsLabel = AnalyticsLabel.Type
  object AnalyticsLabel extends Subtype[String]:
    private final val regex       = "^[a-zA-Z0-9-_.~%]{1,50}$"
    override inline def assertion = matches(regex)
