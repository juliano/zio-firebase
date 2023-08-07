package zio.firebase.firestore

import com.google.cloud.firestore.{CollectionReference, Query}
import zio.Task

trait ZFirestore:
  def get[A](c: CollectionPath, d: DocumentPath)(using JavaCodec[A]): Task[A]

  def set[A](c: CollectionPath, d: DocumentPath, data: A)(using JavaCodec[A]): Task[Unit]

  def setField(c: CollectionPath, d: DocumentPath, field: String, value: Any): Task[Unit]

  def increment(c: CollectionPath, d: DocumentPath, field: String, value: Long): Task[Unit]

  def delete(c: CollectionPath, d: DocumentPath): Task[Unit]

  def query[A](c: CollectionPath)(f: CollectionReference => Query)(using JavaCodec[A]): Task[List[(String, A)]]

  def count(c: CollectionPath)(f: CollectionReference => Query): Task[Long]
