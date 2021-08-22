package demo.base

import java.io.StringWriter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

trait HttpServer {
  private val appConfig = ConfigFactory.load()

  private val route =
    path("metrics") {
      get {
        complete {
          ("TODO")
        }
      }
    }

  def startServer()(implicit system: ActorSystem): Unit = {
    Http().bindAndHandle(route, "0.0.0.0", appConfig.getInt("http-port"))
  }
}
