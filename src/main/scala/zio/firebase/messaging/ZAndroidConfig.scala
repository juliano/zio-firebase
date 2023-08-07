package zio.firebase.messaging

import com.google.firebase.messaging.{AndroidConfig, AndroidNotification}
import zio.{Duration, IO, ZIO}

object ZAndroidConfig:
  def make(ttl: Duration): IO[IllegalArgumentException, AndroidConfig] =
    make(ttl, AndroidNotification.builder)

  def make(
    ttl: Duration,
    color: Color,
    icon: Icon,
    image: Image
  ): IO[IllegalArgumentException, AndroidConfig] =
    make(ttl, AndroidNotification.builder.setColor(color).setIcon(icon).setImage(image))

  private def make(
    ttl: Duration,
    builder: => AndroidNotification.Builder
  ): IO[IllegalArgumentException, AndroidConfig] =
    (for
      notification <- ZIO.attempt(builder.build)
      config <- ZIO.attempt {
                  AndroidConfig.builder.setTtl(ttl.toMillis).setNotification(notification).build
                }
    yield config).refineToOrDie
