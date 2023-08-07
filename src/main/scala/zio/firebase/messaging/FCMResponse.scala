package zio.firebase.messaging

import com.google.firebase.messaging.{BatchResponse, FirebaseMessagingException}
import zio.Chunk

final case class FCMResponse(
  successes: Int,
  failures: Int,
  details: Chunk[Either[FirebaseMessagingException, String]]
)

object FCMResponse:
  def apply(b: BatchResponse): FCMResponse =
    FCMResponse(
      b.getSuccessCount,
      b.getFailureCount,
      Chunk.fromJavaIterable(b.getResponses).map {
        case r if r.isSuccessful => Right(r.getMessageId)
        case r                   => Left(r.getException)
      }
    )
