package zio.firebase

import zio.prelude.Subtype

package object firestore:
  type CollectionPath = CollectionPath.Type
  object CollectionPath extends Subtype[String]

  type DocumentPath = DocumentPath.Type
  object DocumentPath extends Subtype[String]
