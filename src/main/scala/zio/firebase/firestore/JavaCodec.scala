package zio.firebase.firestore

trait JavaCodec[T]:
  def to(data: T): java.util.Map[String, Object]
  def from(map: java.util.Map[String, Object]): Either[Throwable, T]
