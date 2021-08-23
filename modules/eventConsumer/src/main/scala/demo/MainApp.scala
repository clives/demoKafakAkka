package demo

import akka.actor.ActorSystem
import demo.base.HttpServer

object MainApp extends HttpServer{
  implicit val system = ActorSystem("EventsConsumer")

  def main(args: Array[String]) = {
    //todo init config / start consumer
    startServer()
  }
}
