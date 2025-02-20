package zio.firebase.storage

import zio.*

import java.nio.file.{Files, Paths}

object TestStorage:
  final case class StorageInMemory(ref: Ref[Map[String, Array[Byte]]]) extends Storage:
    def upload(blob: String, filePath: String, contentType: ContentType): Task[Unit] =
      upload(blob, Files.readAllBytes(Paths.get(filePath)), contentType)

    def upload(blob: String, content: Array[Byte], contentType: ContentType): Task[Unit] =
      ref.update(_ + (blob -> content))

    def download(blob: String): Task[Array[Byte]] =
      ref.get.map(store => store(blob))

  val layer: ULayer[Storage] = ZLayer {
    Ref.make(Map.empty[String, Array[Byte]]).map(StorageInMemory.apply)
  }