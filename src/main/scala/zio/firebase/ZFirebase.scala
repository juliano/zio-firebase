package zio.firebase

import com.google.firebase.{FirebaseApp, FirebaseOptions}
import zio.*
import zio.firebase.auth.oauth2.Credentials

object ZFirebase:
  def make(credentials: Credentials, bucket: String): RIO[Scope, FirebaseApp] =
    val acquire: Task[FirebaseApp] =
      for
        creds <- credentials.get
        app <- ZIO.attempt {
                 FirebaseApp.initializeApp(
                   FirebaseOptions.builder
                     .setCredentials(creds)
                     .setStorageBucket(bucket)
                     .build
                 )
               }
      yield app

    val release = (fa: FirebaseApp) => ZIO.succeed(fa.delete())

    ZIO.acquireRelease(acquire)(release)

  def live(bucket: String): RLayer[Credentials, FirebaseApp] = ZLayer.scoped {
    for
      credentials <- ZIO.service[Credentials]
      fa          <- make(credentials, bucket)
    yield fa
  }
