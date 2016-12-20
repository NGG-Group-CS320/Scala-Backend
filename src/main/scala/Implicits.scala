package mfdat

object Implicits {
  implicit class RichVector[A](vec: Vector[A]) {
    def dot(other: Vector[A])(implicit numeric: Numeric[A]): A = vec.zip(other).map {
      case (lhs, rhs) => numeric.times(lhs, rhs)
    }.foldRight(numeric.zero) {
      case (term, acc) => numeric.plus(term, acc)
    }
  }

  implicit class RichDouble(v: Double) {
    def toHumanReadable(): Int = (800 * v).floor.toInt
  }
}
