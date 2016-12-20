package mfdat

object Errors {
  case object Unreachable extends RuntimeException("This code should be unreachable!")
}
