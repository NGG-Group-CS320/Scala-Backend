package mfdat

import java.io.FileInputStream
import java.util.Properties

case class Config(path: String) {
  val properties = new Properties
  properties.load(new FileInputStream(path))

  def host: Option[String] = Option(properties.getProperty("host"))
  def database: Option[String] = Option(properties.getProperty("dbname"))
  def primaryTable: Option[String] = Option(properties.getProperty("primarytable"))
  def maxBandwidthTable: Option[String] = Option(properties.getProperty("bwtable"))
  def scoresTable: Option[String] = Option(properties.getProperty("scorestable"))
  def username: Option[String] = Option(properties.getProperty("user"))
  def password: Option[String] = Option(properties.getProperty("password"))

  def bindHost: Option[String] = Option(properties.getProperty("bindhost"))
  def bindPort: Option[Int] = Option(properties.getProperty("bindport")).map(_.toInt)
}
