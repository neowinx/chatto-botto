package com.stackbuilders


import akka.actor.typed._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
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
            case Some(handle: ActorRef[PostMessage]) => handle ! PostMessage(message)
            case _ => 
              context.log.info(s"No handler found for posting message: $message")
              context.log.info(s"\nPlease enter the /login command to start a chat session or /help for command list")
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

  private case class BoostrapData(name: String, client: ActorRef[SessionEvent])

  def apply(): Behavior[SaySomething] =
    Behaviors.setup { context =>
      val chatRoom = context.spawn(ChatRoom(), "chatroom")
      val client = context.spawn(ChattoBottoClient(), "chatobottoclient")

      clientBootstrap(context, chatRoom, client, None)
    }

  //TODO: Probably should define some of these as implicit parameters to diminish the code noise
  def clientBootstrap(context: ActorContext[SaySomething],
                      chatRoom: ActorRef[RoomCommand],
                      client: ActorRef[SessionEvent],
                      maybeBoostrapData: Option[BoostrapData]): Behavior[SaySomething] =
      Behaviors.receiveMessage { message =>
        message.message match {
          case s"/login $name" =>
            chatRoom ! ChatRoom.GetSession(name, client)
            clientBootstrap(
              context, chatRoom, client,
              Some(BoostrapData(name, client)))
          case s"/quit" =>
            maybeBoostrapData match {
              case Some(boostrapData) => chatRoom ! ChatRoom.Disconnect(boostrapData.name)
              case _ => context.log.error(s"cannot logout from session. Session not started?")
            }
            Behaviors.same
          case s"/help" =>
            println("\ncommands list:")
            println("/login {NAME} - Start chatbot session")
            println("/quit - Quits chatto-botto")
            Behaviors.same
          case s"$message" =>
            if (message.nonEmpty)
              client ! Post(message)
            else
              println("\ntype '/help' for command list")
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

