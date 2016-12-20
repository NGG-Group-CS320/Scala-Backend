package mfdat

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

object Database {
  val config = Config("database.cfg")
  require(config.host.nonEmpty, "Configuration file was missing host property.")
  require(config.database.nonEmpty, "Configuration file was missing dbname property.")
  require(config.username.nonEmpty, "Configuration file was missing user property.")
  require(config.password.nonEmpty, "Configuration file was missing password property.")

  val driver = Class.forName("org.postgresql.Driver")
  val dbUrl = s"jdbc:postgresql://${config.host.get}/${config.database.get}"
  val conn = DriverManager.getConnection(dbUrl, config.username.get, config.password.get)

  // Executes the specified query immediately, returning the result.
  def exec(query: String): ResultSet = conn.createStatement().executeQuery(query)

  // Creates a prepared statement.
  def prepare(query: String): PreparedStatement = conn.prepareStatement(query)

  val batchBuf = conn.createStatement()

  // Adds the specified query to the batch buffer.
  def batch(query: String): Unit = batchBuf.addBatch(query)
  // Executes all the queries in the batch.
  def execBatch(): Unit = batchBuf.executeBatch()

  // Closes the database connection.
  def close(): Unit = conn.close()
}
