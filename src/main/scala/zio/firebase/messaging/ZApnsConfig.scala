package zio.firebase.messaging

import com.google.firebase.messaging.{ApnsConfig, Aps}
import zio.{IO, ZIO}

object ZApnsConfig:
  def make: IO[IllegalArgumentException, ApnsConfig] =
    (for
      aps    <- ZIO.attempt(Aps.builder.build)
      config <- ZIO.attempt(ApnsConfig.builder.setAps(aps).build)
    yield config).refineToOrDie
