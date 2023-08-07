package zio.firebase.firestore

import com.google.api.core.{ApiFuture, ApiFutureToListenableFuture}
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import zio.interop.guava.fromListenableFuture
import zio.*

import scala.jdk.CollectionConverters.*

final case class ZFirestoreLive(firestore: Firestore) extends ZFirestore:
  def get[A](c: CollectionPath, d: DocumentPath)(using codec: JavaCodec[A]): Task[A] =
    for
      doc  <- withFirestore(_.collection(c).document(d).get())
      data <- ZIO.fromEither(codec.from(doc.getData))
    yield data

  def set[A](c: CollectionPath, d: DocumentPath, data: A)(using codec: JavaCodec[A]): Task[Unit] =
    withFirestore(_.collection(c).document(d).set(codec.to(data), SetOptions.merge())).unit

  def setField(c: CollectionPath, d: DocumentPath, field: String, value: Any): Task[Unit] =
    withFirestore(
      _.collection(c)
        .document(d)
        .set(Map(field -> value).asJava)
    ).unit

  def increment(c: CollectionPath, d: DocumentPath, field: String, value: Long): Task[Unit] =
    withFirestore(
      _.collection(c)
        .document(d)
        .update(field, FieldValue.increment(value))
    ).unit

  def delete(c: CollectionPath, d: DocumentPath): Task[Unit] =
    withFirestore(_.collection(c).document(d).delete()).unit

  def query[A](
    path: CollectionPath
  )(f: CollectionReference => Query)(implicit codec: JavaCodec[A]): Task[List[(String, A)]] =
    for
      query <- withFirestore(fs => f(fs.collection(path)).get())
      list   = query.getDocuments.asScala.toList.map(d => (d.getId, d.getData))
      result <- ZIO
                  .validatePar(list) { (id, data) =>
                    ZIO.fromEither(codec.from(data)).map((id, _))
                  }
                  .mapError(e => new IllegalArgumentException(e.mkString("\n")))
    yield result

  def count(path: CollectionPath)(f: CollectionReference => Query): Task[Long] =
    withFirestore(fs => f(fs.collection(path)).count().get()).map(_.getCount())

  private def withFirestore[A](f: Firestore => ApiFuture[A]): Task[A] =
    fromListenableFuture(ZIO.succeed(new ApiFutureToListenableFuture[A](f(firestore))))

object ZFirestoreLive:
  val live: URLayer[FirebaseApp, ZFirestore] = ZLayer {
    for
      app       <- ZIO.service[FirebaseApp]
      firestore <- ZIO.attempt(FirestoreClient.getFirestore(app)).orDie
    yield ZFirestoreLive(firestore)
  }
