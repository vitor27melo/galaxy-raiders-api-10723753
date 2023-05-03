
@file:Suppress("UNUSED_PARAMETER") // <- REMOVE
package galaxyraiders.core.physics

data class Point2D(val x: Double, val y: Double) {
  operator fun plus(p: Point2D): Point2D {
    return Point2D(x + p.x, y + p.y)
  }

  operator fun plus(v: Vector2D): Point2D {
    return Point2D(x + v.dx, y + v.dy)
  }

  override fun toString(): String {
    return "Point2D(x=$x, y=$y)"
  }

  fun toVector(): Vector2D {
    return Vector2D(x, y)
  }

  fun impactVector(p: Point2D): Vector2D {
    // val vetor = Vector2D(Math.abs(x - p.x), Math.abs(y - p.y))
    // print("VALOR X: ${vetor.dx} VALOR X.NORMAL: ${vetor.normal.dx}")
    // return vetor
    return Vector2D(Math.abs(x - p.x), Math.abs(y - p.y))
  }

  fun impactDirection(p: Point2D): Vector2D {
    return INVALID_VECTOR
  }

  fun contactVector(p: Point2D): Vector2D {
    //ñ tá certo
    return Vector2D(Math.abs(x - p.x), Math.abs(y - p.y))
  }

  fun contactDirection(p: Point2D): Vector2D {
    return INVALID_VECTOR
  }

  fun distance(p: Point2D): Double {
    return INVALID_DOUBLE
  }
}
