package zio.firebase.messaging

import com.google.firebase.messaging.{Message, MulticastMessage}
import zio.{IO, NonEmptyChunk, ZIO}

import scala.jdk.CollectionConverters.*

object ZMessage:
  def make(content: FCMContent, token: DeviceToken): IO[IllegalArgumentException, Message] =
    for
      notification <- ZNotification.make(content.title, content.body)
      config        = content.config
      android      <- ZAndroidConfig.make(config.ttl, config.color, config.icon, config.image)
      apns         <- ZApnsConfig.make
      message <- ZIO.attempt {
                   Message.builder
                     .setToken(token)
                     .setNotification(notification)
                     .setAndroidConfig(android)
                     .setApnsConfig(apns)
                     .putAllData(content.data.asJava)
                     .build
                 }
                   .refineToOrDie[IllegalArgumentException]
    yield message

  def make(
    content: FCMContent,
    tokens: NonEmptyChunk[DeviceToken]
  ): IO[IllegalArgumentException, MulticastMessage] =
    for
      notification <- ZNotification.make(content.title, content.body)
      config        = content.config
      android      <- ZAndroidConfig.make(config.ttl, config.color, config.icon, config.image)
      apns         <- ZApnsConfig.make
      message <- ZIO.attempt {
                   MulticastMessage.builder
                     .addAllTokens(tokens.map(_.toString).toList.asJava)
                     .setNotification(notification)
                     .setAndroidConfig(android)
                     .setApnsConfig(apns)
                     .putAllData(content.data.asJava)
                     .build
                 }
                   .refineToOrDie[IllegalArgumentException]
    yield message
