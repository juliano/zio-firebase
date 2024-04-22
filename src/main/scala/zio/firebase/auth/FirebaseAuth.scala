package zio.firebase.auth

import com.google.firebase.auth.{FirebaseToken, UserInfo}
import zio.*

trait FirebaseAuth:
  def createCustomToken(uid: String): Task[String]

  def verifyToken(token: String): Task[FirebaseToken]

  def createUser(uid: String, email: String, password: String): Task[UserInfo]

  def updateUser(uid: String, email: Option[String], phoneNumber: Option[String]): Task[UserInfo]

  def getUserByEmail(email: String): Task[UserInfo]
