package zio.firebase

import com.google.firebase.{FirebaseApp, FirebaseOptions}
import zio.*
import zio.firebase.auth.oauth2.Credentials
import zio.firebase.storage.BucketName

object ZFirebase:
  def make(credentials: Credentials, bucketName: BucketName): RIO[Scope, FirebaseApp] =
    val acquire: Task[FirebaseApp] =
      for
        creds <- credentials.get
        app <- ZIO.attempt {
                 FirebaseApp.initializeApp(
                   FirebaseOptions.builder
                     .setCredentials(creds)
                     .setStorageBucket(bucketName.value)
                     .build
                 )
               }
      yield app

    val release = (fa: FirebaseApp) => ZIO.succeed(fa.delete())

    ZIO.acquireRelease(acquire)(release)

  val live: RLayer[Credentials & BucketName, FirebaseApp] = ZLayer.scoped {
    for
      credentials <- ZIO.service[Credentials]
      bucketName  <- ZIO.service[BucketName]
      fa          <- make(credentials, bucketName)
    yield fa
  }
