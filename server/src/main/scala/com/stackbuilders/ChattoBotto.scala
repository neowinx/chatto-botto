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


object ChattoBottoServer {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      context.spawn(ChatRoom(), "chatroom")
      Behaviors.same
    }
}


object Server extends App {
  val system = ActorSystem(ChattoBottoServer(), "ChatRoomDemo")
  val cluster = Cluster(system)
}
