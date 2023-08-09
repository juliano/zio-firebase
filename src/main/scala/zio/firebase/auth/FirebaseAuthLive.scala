package zio.firebase.auth

import com.google.api.core.{ApiFuture, ApiFutureToListenableFuture}
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.{FirebaseAuth as GoogleFirebaseAuth, *}
import zio.*
import zio.interop.guava.fromListenableFuture

final case class FirebaseAuthLive(auth: GoogleFirebaseAuth) extends FirebaseAuth:
  def createCustomToken(uid: String): Task[String] = withFirebaseAuth(_.createCustomTokenAsync(uid))

  def verifyToken(token: String): Task[FirebaseToken] = withFirebaseAuth(_.verifyIdTokenAsync(token))

  def createUser(uid: String, email: String, password: String): Task[UserInfo] = withFirebaseAuth(
    _.createUserAsync(
      new UserRecord.CreateRequest()
        .setUid(uid)
        .setEmail(email)
        .setEmailVerified(true)
        .setPassword(password)
    )
  )

  def getUserByEmail(email: String): Task[UserInfo] = withFirebaseAuth(_.getUserByEmailAsync(email))

  private def withFirebaseAuth[A](f: GoogleFirebaseAuth => ApiFuture[A]): Task[A] =
    fromListenableFuture(ZIO.succeed(new ApiFutureToListenableFuture[A](f(auth))))

object FirebaseAuthLive:
  val layer: URLayer[FirebaseApp, FirebaseAuth] = ZLayer {
    for
      app  <- ZIO.service[FirebaseApp]
      auth <- ZIO.attempt(GoogleFirebaseAuth.getInstance(app)).orDie
    yield FirebaseAuthLive(auth)
  }
