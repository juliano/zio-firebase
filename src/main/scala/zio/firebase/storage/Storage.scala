package zio.firebase.storage

import zio.Task

trait Storage:
  def upload(blob: String, filePath: String, contentType: ContentType): Task[Unit]

  def upload(blob: String, content: Array[Byte], contentType: ContentType): Task[Unit]

  def download(blob: String): Task[Array[Byte]]
