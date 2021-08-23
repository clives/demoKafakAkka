package demo.base

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
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
