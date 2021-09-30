package com.stackbuilders


import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.stackbuilders.ChatRoom._

object ChattoBottoClient {

  def apply(): Behavior[SessionEvent] =
    clientSession(None)

  private def clientSession(poster: Option[ActorRef[PostMessage]]):Behavior[SessionEvent] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case SessionGranted(handle) =>
          handle ! PostMessage("Welcome to ChattoBotto")
          clientSession(Some(handle))
        case Post(message) =>
          poster match {
            case Some(handle: ActorRef[PostMessage]) =>
              handle ! PostMessage(message)
          }
          Behaviors.same
        case SessionDenied(reason) =>
          context.log.info(s"session denied. $reason")
          Behaviors.stopped
        case MessagePosted(screenName, message) =>
          context.log.info(s"message has been posted by '$screenName': $message")
          Behaviors.same
      }
    }
}

object ChattoBottoClientBootstrap {
  
  final case class SaySomething(message: String)
  
  def apply(): Behavior[SaySomething] =
    Behaviors.setup { context =>
      val chatRoom = context.spawn(ChatRoom(), "chatroom")
      val client = context.spawn(ChattoBottoClient(), "chatobottoclient")

      Behaviors.receiveMessage { message =>
        message.message match {
          case s"/login $name" =>
            chatRoom ! ChatRoom.GetSession(name, client)
          case s"/post $rest" =>
            client ! Post(rest)
          case s"/help" =>
            println("\ncommands list:")
            println("/login {NAME} - Start chatbot session")
          case _ =>
            println("type '/help' for command list")
        }
        Behaviors.same
      }
    }

}

object Client extends App {
  import scala.io.StdIn
  
  val system = ActorSystem(ChattoBottoClientBootstrap(), "ChattoBottoSystem")
  val cluster = Cluster(system)
  
  while (true) {
    print(":")
    val command = StdIn.readLine()
    system ! ChattoBottoClientBootstrap.SaySomething(command)
  }
}

