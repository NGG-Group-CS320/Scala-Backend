package mfdat

import scala.io.StdIn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object WebServer extends App {
  implicit val system = ActorSystem("mfdat")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route =
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

  def wheelRequest(ids: Set[Int]): String = ???

  def lineRequest(ids: Set[Int]): String = ???

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"API server is now online at http://localhost:8080/\nPress RETURN to stop...")
  // Run until new line is read.
  StdIn.readLine()

  // Unbind port, shutdown actor system, and close database connection.
  bindingFuture.flatMap(_.unbind()).onComplete(_ => {
    system.terminate()
    Database.close()
  })
}
