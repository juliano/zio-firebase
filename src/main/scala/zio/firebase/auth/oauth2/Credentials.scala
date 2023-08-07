package zio.firebase.auth.oauth2

import com.google.auth.oauth2.GoogleCredentials
import zio.*

import java.io.{IOException, InputStream}

trait Credentials extends Serializable:
  def get: IO[IOException, GoogleCredentials]

object Credentials:
  case object DefaultCredentials extends Credentials:
    def get: IO[IOException, GoogleCredentials] =
      ZIO
        .attemptBlocking(
          GoogleCredentials.getApplicationDefault
        )
        .refineToOrDie[IOException]

  val default: Layer[IOException, Credentials] =
    ZLayer.succeed[Credentials](DefaultCredentials)

  final case class FromInputStream(is: InputStream) extends Credentials:
    def get: IO[IOException, GoogleCredentials] =
      ZIO
        .attemptBlocking(
          GoogleCredentials.fromStream(is)
        )
        .refineToOrDie[IOException]

  val fromInputStream: ZLayer[InputStream, IOException, Credentials] =
    ZLayer.fromFunction(FromInputStream.apply)
