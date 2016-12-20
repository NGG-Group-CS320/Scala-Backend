package mfdat

import java.sql.{ResultSet, Timestamp}

import Errors._
import Implicits._

object PerformanceData {
  def stringify(n: Double): String = if (n > 0 && n < 1) {
    n.toString.replace('.', '_').take(5)
  } else {
    n.toInt.toString
  }

  def nextFrom(rs: ResultSet): Option[PerformanceData] = if (rs.next()) {
    val (from, to) = (rs.getTimestamp("from"), rs.getTimestamp("to"))
    val arr = Vector.range(-4, 16).map(math.pow(2, _))
    val writes = arr.map(n => rs.getDouble(s"writes${stringify(n)}msPct"))
    val reads = arr.map(n => rs.getDouble(s"reads${stringify(n)}msPct"))
    val cpu = rs.getDouble("cpuLatestTotalAvgPct")
    val bandwidth = rs.getDouble("normalizedbandwidth")
    val delAckPct = rs.getDouble("delAcksPct")
    Some(PerformanceData(from, to, writes, reads, cpu / 100, bandwidth, delAckPct / 100))
  } else {
    None
  }
}

case class PerformanceData(from: Timestamp, to: Timestamp, writes: Vector[Double], reads: Vector[Double],
                           cpu: Double, bandwidth: Double, delAckPct: Double) {
  require(writes.length == 20, s"Writes vector was incorrectly sized (Expected: 20, found: ${writes.length}).")
  require(reads.length == 20, s"Reads vector was incorrectly sized (Expected: 20, found: ${reads.length}).")

  // Constant vector for computing scores from frequency vectors for read and write scores.
  val freqWeights = Vector.range(20, 0, -1).map(_ / 20.0)
  // Constant vector for computing weighted average in health score.
  val avgWeights = Vector(0.45, 0.25, 0.2, 0.1)

  // Computes the write score for this specific entry.
  def writeScore: Double = writes.map(_ / 100).dot(freqWeights)

  // Computes the read score for this specific entry.
  def readScore: Double = reads.map(_ / 100).dot(freqWeights)

  // Computes the CPU-Bandwidth score for this specific entry using Euclidean distance.
  def cpuBandwidthScore: Double = math.sqrt(math.pow(1 - cpu, 2) + math.pow(bandwidth, 2) / 2)

  // Computes the interim health score for this specific entry.
  def interimHealthScore: Double = Vector(writeScore, readScore, cpuBandwidthScore, 1 - delAckPct).dot(avgWeights)

  // Computes the human readable health score for this specific entry.
  def healthScore: Int = interimHealthScore.toHumanReadable

  // Computes the color according to the color coding rules for health scores.
  def color: String = (healthScore / 100) match {
    case 0 => "#c0392b"
    case 1 => "#e74c3c"
    case 2 => "#d35400"
    case 3 => "#e67e22"
    case 4 => "#f39c12"
    case 5 => "#f1c40f"
    case 6 => "#2ecc71"
    case 7 => "#27ae60"
    case 8 => "#27ae60"
    case _ => throw Unreachable
  }
}
