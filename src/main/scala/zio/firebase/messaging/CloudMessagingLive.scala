package zio.firebase.messaging

import com.google.api.core.{ApiFuture, ApiFutureToListenableFuture}
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import zio.*
import zio.interop.guava.fromListenableFuture

final case class CloudMessagingLive(messaging: FirebaseMessaging) extends CloudMessaging:
  def send(content: FCMContent, token: DeviceToken): Task[String] =
    for
      message <- ZMessage.make(content, token)
      id      <- withMessaging(_.sendAsync(message))
    yield id

  def send(content: FCMContent, tokens: NonEmptyChunk[DeviceToken]): Task[FCMResponse] =
    for
      message  <- ZMessage.make(content, tokens)
      response <- withMessaging(_.sendMulticastAsync(message))
    yield FCMResponse(response)

  private def withMessaging[A](f: FirebaseMessaging => ApiFuture[A]): Task[A] =
    fromListenableFuture(ZIO.succeed(new ApiFutureToListenableFuture[A](f(messaging))))

object CloudMessagingLive:
  val layer: URLayer[FirebaseApp, CloudMessaging] = ZLayer {
    for
      app       <- ZIO.service[FirebaseApp]
      messaging <- ZIO.attempt(FirebaseMessaging.getInstance(app)).orDie
    yield CloudMessagingLive(messaging)
  }
