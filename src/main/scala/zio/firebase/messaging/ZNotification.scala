package zio.firebase.messaging

import com.google.firebase.messaging.Notification
import zio.{IO, ZIO}

object ZNotification:
  def make(title: Title, body: Body): IO[IllegalArgumentException, Notification] =
    ZIO.attempt {
      Notification.builder.setTitle(title).setBody(body).build
    }.refineToOrDie
