package zio.firebase.messaging

import zio.Duration

final case class FCMConfig(color: Color, icon: Icon, image: Image, ttl: Duration)
