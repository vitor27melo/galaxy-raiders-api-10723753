package galaxyraiders.core.game
import java.time.Instant

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

class Explosion(
  initialPosition: Point2D,
  initialVelocity: Vector2D,
  radius: Double,
  mass: Double,
  val creationTime: Long,
) :
  SpaceObject("Explosion", '.', initialPosition, initialVelocity, radius, mass) {
    // Explosões têm duração de 1 segundo
    fun shouldStillExist() : Boolean {
      var now = Instant.now().getEpochSecond()
      var diff = now - this.creationTime
      return diff < 1
    }
  }
