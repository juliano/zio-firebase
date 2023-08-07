package zio.firebase.messaging

import zio.{NonEmptyChunk, Task}

trait CloudMessaging:
  def send(content: FCMContent, token: DeviceToken): Task[String]
  def send(content: FCMContent, tokens: NonEmptyChunk[DeviceToken]): Task[FCMResponse]
