package zio.firebase.messaging

import com.google.firebase.messaging.FirebaseMessagingException

final case class MessageDeliveryException(ex: FirebaseMessagingException) extends Exception(ex)
