package zio.firebase.auth

import com.google.api.core.{ApiFuture, ApiFutureToListenableFuture}
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*
import zio.*
import zio.interop.guava.fromListenableFuture

final case class ZFirebaseAuthLive(firebaseAuth: FirebaseAuth) extends ZFirebaseAuth:
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

  private def withFirebaseAuth[A](f: FirebaseAuth => ApiFuture[A]): Task[A] =
    fromListenableFuture(ZIO.succeed(new ApiFutureToListenableFuture[A](f(firebaseAuth))))

object ZFirebaseAuthLive:
  val layer: URLayer[FirebaseApp, ZFirebaseAuth] = ZLayer {
    for
      app  <- ZIO.service[FirebaseApp]
      auth <- ZIO.attempt(FirebaseAuth.getInstance(app)).orDie
    yield ZFirebaseAuthLive(auth)
  }
