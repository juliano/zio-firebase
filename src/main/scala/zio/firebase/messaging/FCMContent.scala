package zio.firebase.messaging

import zio.prelude.Validation

final case class FCMContent(
  title: Title,
  body: Body,
  data: Map[String, String],
  config: FCMConfig
)

object FCMContent:
  def make(title: String, body: String, config: FCMConfig): Validation[String, FCMContent] =
    make(title, body, Map(), config)

  def make(
    title: String,
    body: String,
    data: Map[String, String],
    config: FCMConfig
  ): Validation[String, FCMContent] =
    for
      title <- Title.make(title)
      body  <- Body.make(body)
    yield FCMContent(title, body, data, config)
