package zio.firebase.storage

import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.StorageClient
import zio.*

import java.nio.file.{Files, Paths}

case class StorageLive(client: StorageClient) extends Storage:
  def upload(blob: String, filePath: String, contentType: ContentType): Task[Unit] =
    upload(blob, Files.readAllBytes(Paths.get(filePath)), contentType)

  def upload(blob: String, content: Array[Byte], contentType: ContentType): Task[Unit] =
    ZIO.attempt(
      client.bucket().create(blob, content, contentType.value)
    )

  def download(blob: String): Task[Array[Byte]] =
    ZIO.attempt(client.bucket().get(blob).getContent())

object StorageLive:
  val layer: URLayer[FirebaseApp, Storage] = ZLayer {
    for
      app    <- ZIO.service[FirebaseApp]
      client <- ZIO.attempt(StorageClient.getInstance(app)).orDie
    yield StorageLive(client)
  }
