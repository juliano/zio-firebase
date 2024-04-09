package zio.firebase.messaging

import zio.prelude.Validation

final case class FCMContent(
  title: Title,
  body: Body,
  data: Map[String, String],
  config: FCMConfig,
  label: Option[AnalyticsLabel]
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
    yield FCMContent(title, body, data, config, None)

  def make(
    title: String,
    body: String,
    data: Map[String, String],
    config: FCMConfig,
    label: Option[String]
  ): Validation[String, FCMContent] =
    for
      title <- Title.make(title)
      body  <- Body.make(body)
      optLabel <- label match
                    case Some(l) => AnalyticsLabel.make(l).map(Some(_))
                    case None    => Validation.succeed(None)
    yield FCMContent(title, body, data, config, optLabel)
