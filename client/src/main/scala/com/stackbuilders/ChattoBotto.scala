//#full-example
package com.stackbuilders


import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import akka.cluster.typed.Cluster
import akka.NotUsed
import akka.actor.typed._
import com.stackbuilders.ChatRoom._
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import akka.cluster.typed.Join


object ChattoBottoClient {
  import ChatRoom._

  def apply(): Behavior[SessionEvent] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case SessionGranted(handle) =>
          handle ! PostMessage("Hello World!")
          Behaviors.same
        case SessionDenied(reason) =>
          context.log.info(s"session denied. $reason")
          Behaviors.stopped
        case MessagePosted(screenName, message) =>
          context.log.info(s"message has been posted by '$screenName': $message")
          Behaviors.stopped
      }
    }
}

object ChattoBottoClientBootstrap {
  import scala.io.StdIn
  
  final case class SaySomething(message: String)
  
  def apply(): Behavior[SaySomething] =
    Behaviors.setup { context =>
      val chatRoom = context.spawn(ChatRoom(), "chatroom")
      val client = context.spawn(ChattoBottoClient(), "chatobottoclient")
      Behaviors.receiveMessage { message =>
        message.message match {
          case "/login" => 
            val name = StdIn.readLine("enter your name:")
            chatRoom ! ChatRoom.GetSession(name, client)
          case _ =>  println("nothing")
        } 
        Behaviors.same
      }
    }

}

object Client extends App {
  import scala.io.StdIn
  
  val system = ActorSystem(ChattoBottoClientBootstrap(), "ChatRoomDemo")
  val cluster = Cluster(system)
  
  println("Hello")
  system ! ChattoBottoClientBootstrap.SaySomething(":login moloko")
}

