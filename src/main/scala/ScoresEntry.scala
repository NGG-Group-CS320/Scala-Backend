package mfdat

import java.sql.{ResultSet, Timestamp}

import Errors._

object ScoresEntry {
  def nextFrom(rs: ResultSet): Option[ScoresEntry] = if (rs.next()) {
    Some(ScoresEntry(
      rs.getInt("systemId"), rs.getTimestamp("from"), rs.getTimestamp("to"), rs.getInt("health_score"),
      rs.getInt("read_score"), rs.getInt("write_score"), rs.getInt("cpu_bandwidth_score"), rs.getDouble("del_ack_pct")
    ))
  } else {
    None
  }
}

case class ScoresEntry(systemId: Int, from: Timestamp, to: Timestamp, healthScore: Int,
                       writeScore: Int, readScore: Int, cpuBandwidthScore: Int, delAckPct: Double) {
  require(healthScore >= 0 && healthScore <= 800, s"Invalid health score found (value $healthScore was not on range [0, 800]).")
  require(writeScore >= 0 && writeScore <= 800, s"Invalid write score found (value $writeScore was not on range [0, 800]).")
  require(readScore >= 0 && readScore <= 800, s"Invalid read score found (value $readScore was not on range [0, 800]).")
  require(cpuBandwidthScore >= 0 && cpuBandwidthScore <= 800, s"Invalid CPU-bandwidth score found (value $cpuBandwidthScore was not on range [0, 800]).")
  require(delAckPct >= 0 && delAckPct <= 800, s"Invalid delayed acknowledgement percentage found (value $delAckPct was not on range [0, 1]).")

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

  def toJSON: String = s"""{
    "id": "$systemId",
    "name": "System $systemId",
    "color": "$color",
    "score": "$healthScore",
    "writeScore": "$writeScore",
    "readScore": "$readScore",
    "cbScore": "$cpuBandwidthScore",
    "delAckPct": "$delAckPct"
  }"""
}
