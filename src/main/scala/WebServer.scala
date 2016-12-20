package mfdat

import scala.io.StdIn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import ch.megard.akka.http.cors.CorsDirectives._

object WebServer extends App {
  implicit val system = ActorSystem("mfdat")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route =
    cors() {
      get {
        path("") {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "MF-DAT RESTful API"))
        } ~
        pathPrefix("wheel" / Remaining) { input =>
          complete(HttpEntity(ContentTypes.`application/json`, wheelRequest(input.split(",").map(_.toInt).toSet)))
        } ~
        pathPrefix("line" / Remaining) { input =>
          complete(HttpEntity(ContentTypes.`application/json`, lineRequest(input.split(",").map(_.toInt).toSet)))
        }
      }
    }

  def wheelRequest(ids: Set[Int]): String = {
    val subQuery = s"""SELECT MAX("from") FROM ${Database.config.scoresTable.get} WHERE systemid = ?"""
    val prepared = Database.prepare(s"""SELECT * FROM ${Database.config.scoresTable.get} WHERE systemid = ? AND "from" = ($subQuery)""")

    def node(name: String, children: String): String = s"""{
      "name": "$name",
      "children": [$children]
    }"""

    val entries = ids.flatMap { id =>
      prepared.setInt(1, id)
      prepared.setInt(2, id)
      val rs = prepared.executeQuery()
      ScoresEntry.nextFrom(rs)
    }

    val quarterOne = entries.filter(entry => entry.healthScore >= 0 && entry.healthScore < 200)
    val quarterTwo = entries.filter(entry => entry.healthScore >= 200 && entry.healthScore < 400)
    val quarterThree = entries.filter(entry => entry.healthScore >= 400 && entry.healthScore < 600)
    val quarterFour = entries.filter(entry => entry.healthScore >= 600 && entry.healthScore <= 800)

    val nodeOne = node("0-200", quarterOne.map(_.toJSON).mkString(",\n"))
    val nodeTwo = node("200-400", quarterTwo.map(_.toJSON).mkString(",\n"))
    val nodeThree = node("400-600", quarterThree.map(_.toJSON).mkString(",\n"))
    val nodeFour = node("600-800", quarterFour.map(_.toJSON).mkString(",\n"))

    val halfOne = node("0-400", Seq(nodeOne, nodeTwo).mkString(",\n"))
    val halfTwo = node("400-800", Seq(nodeThree, nodeFour).mkString(",\n"))

    s"[$halfOne, $halfTwo]"
  }

  def lineRequest(ids: Set[Int]): String = {
    val prepared = Database.prepare(s"SELECT * FROM ${Database.config.scoresTable.get} WHERE systemid = ?")

    val systems = ids.map { id =>
      var entries: Set[ScoresEntry] = Set()

      prepared.setInt(1, id)
      val rs = prepared.executeQuery()

      var last = ScoresEntry.nextFrom(rs)
      while (last.nonEmpty) {
        entries = entries + last.get
        last = ScoresEntry.nextFrom(rs)
      }

      (id, entries)
    }

    def node(pair: (Int, Set[ScoresEntry])): String = pair match {
      case (id, entries) => {
        val data = entries.map(_.toJSONPoint).mkString(",\n")
        s"""{
          "id": "$id",
          "name": "System $id",
          "data": [$data]
        }"""
      }
    }

    ???
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8081)

  println(s"API server is now online at http://localhost:8081/\nPress RETURN to stop...")
  // Run until new line is read.
  StdIn.readLine()

  // Unbind port, shutdown actor system, and close database connection.
  bindingFuture.flatMap(_.unbind()).onComplete(_ => {
    system.terminate()
    Database.close()
  })
}
