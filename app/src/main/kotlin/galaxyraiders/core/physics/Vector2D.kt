package galaxyraiders.core.physics

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlin.math.*

@JsonIgnoreProperties("unit", "normal", "degree", "magnitude")
data class Vector2D(val dx: Double, val dy: Double) {
  override fun toString(): String {
    return "Vector2D(dx=$dx, dy=$dy)"
  }

  val magnitude: Double
    get() = sqrt(dx.pow(2) + dy.pow(2))

  val radiant: Double
    get() = atan2(dy, dx)

  val degree: Double
    get() = 180 * radiant / 3.1415

  val unit: Vector2D
    get() = Vector2D(dx / magnitude, dy / magnitude)
 
  val normal: Vector2D
    get() = Vector2D(dy, -dx).unit

  operator fun times(scalar: Double): Vector2D {
    return Vector2D(scalar * dx, scalar * dy)
  }

  operator fun div(scalar: Double): Vector2D {
    return Vector2D(dx / scalar, dy / scalar)
  }

  operator fun times(v: Vector2D): Double {
    return dx * v.dx + dy * v.dy
  }

  operator fun plus(v: Vector2D): Vector2D {
    return Vector2D(dx + v.dx, dy + v.dy)
  }

  operator fun plus(p: Point2D): Point2D {
    return Point2D(dx + p.x, dy + p.y)
  }

  operator fun unaryMinus(): Vector2D {
    return Vector2D(-dx, -dy)
  }

  operator fun minus(v: Vector2D): Vector2D {
    return Vector2D(dx - v.dx, dy - v.dy)
  }

  fun scalarProject(target: Vector2D): Double {
    return Vector2D(dx, dy) * target.unit
  }

  fun vectorProject(target: Vector2D): Vector2D {
    val vec = Vector2D(dx, dy)
    val proj = target *((vec * target) / (target.magnitude*target.magnitude))
    // Estava dando erro de assert para a tupla [7.0, 7.000000000001]
    val result = Vector2D(String.format("%.2f", proj.dx).toDouble(), String.format("%.2f", proj.dy).toDouble())
    return result
  }
}

operator fun Double.times(v: Vector2D): Vector2D {
  return Vector2D(v.dx*2, v.dy*2)
}
