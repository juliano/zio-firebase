package zio.firebase.auth

import com.google.firebase.auth.*
import zio.*

trait ZFirebaseAuth:
  def createCustomToken(uid: String): Task[String]

  def verifyToken(token: String): Task[FirebaseToken]

  def createUser(uid: String, email: String, password: String): Task[UserInfo]

  def getUserByEmail(email: String): Task[UserInfo]
