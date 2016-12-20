package mfdat

import Implicits._

object HealthScoreCalculator extends App {
  require(Database.config.primaryTable.nonEmpty, "Configuration file was missing primarytable property.")
  require(Database.config.scoresTable.nonEmpty, "Configuration file was missing scorestable property.")

  val systemIdResults = Database.exec(s"SELECT systemid FROM ${Database.config.primaryTable.get} GROUP BY systemid")
  val systemIds = {
    var tmp: Set[Int] = Set()
    while (systemIdResults.next()) {
      tmp = tmp + systemIdResults.getInt("systemid")
    }
    tmp
  }

  println("[BEGIN] Health Score Computation")

  for (systemId <- systemIds) {
    println(s"[BEGIN] Computing health scores for $systemId.")

    val arr = Seq.range(-4, 16).map(math.pow(2, _)).toSet
    val writeCols = arr.map(n => s"writes${PerformanceData.stringify(n)}msPct")
    val readCols = arr.map(n => s"reads${PerformanceData.stringify(n)}msPct")
    val fields = Set("\"from\"", "\"to\"", "cpuLatestTotalAvgPct", "normalizedbandwidth", "delAcksPct") ++ writeCols ++ readCols
    val queryFields = fields.mkString(",")

    val results = Database.exec(s"SELECT $queryFields FROM ${Database.config.primaryTable.get} WHERE systemid = $systemId")

    val insertFields = """(systemid, "from", "to", health_score, write_score, read_score, cpu_bandwidth_score, del_ack_pct)"""
    val insert = Database.prepare(s"""INSERT INTO ${Database.config.scoresTable.get} $insertFields VALUES (?, ?, ?, ?, ?, ?, ?, ?)""")

    var count = 0
    var last: Option[PerformanceData] = None
    do {
      last = PerformanceData.nextFrom(results)
      count += 1

      last match {
        case Some(data) => {
          insert.setInt(1, systemId)
          insert.setTimestamp(2, data.from)
          insert.setTimestamp(3, data.to)
          insert.setInt(4, data.healthScore)
          insert.setInt(5, data.writeScore.toHumanReadable)
          insert.setInt(6, data.readScore.toHumanReadable)
          insert.setInt(7, data.cpuBandwidthScore.toHumanReadable)
          insert.setDouble(8, data.delAckPct)

          insert.addBatch()
        }
        case None => ()
      }

      if (count % 100 == 0) {
        println(s"[INFO] Processed $count entries so far.")
      }
    } while (last.nonEmpty)

    // Execute all the insertions.
    insert.executeBatch()

    println(s"[COMPLETE] Computing health scores for $systemId.")
  }
  println("[COMPLETE] Health Score Computation")
}
