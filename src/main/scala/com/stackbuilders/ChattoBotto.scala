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


object ChatRoom {
  sealed trait RoomCommand
  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends RoomCommand

  sealed trait SessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  final case class MessagePosted(screenName: String, message: String) extends SessionEvent

  sealed trait SessionCommand
  final case class PostMessage(message: String) extends SessionCommand
  private final case class NotifyClient(message: MessagePosted) extends SessionCommand
  
  private final case class PublishSessionMessage(screenName: String, message: String) extends RoomCommand

  def apply(): Behavior[RoomCommand] =
    chatRoom(List.empty)

  private def chatRoom(sessions: List[ActorRef[SessionCommand]]): Behavior[RoomCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case GetSession(screenName, client) =>
          // create a child actor for further interaction with the client
          val ses = context.spawn(
            session(context.self, screenName, client),
            name = URLEncoder.encode(screenName, StandardCharsets.UTF_8.name))
          client ! SessionGranted(ses)
          chatRoom(ses :: sessions)
        case PublishSessionMessage(screenName, message) =>
          val notification = NotifyClient(MessagePosted(screenName, message))
          sessions.foreach(_ ! notification)
          Behaviors.same
      }
    }

  private def session(
      room: ActorRef[PublishSessionMessage],
      screenName: String,
      client: ActorRef[SessionEvent]): Behavior[SessionCommand] =
    Behaviors.receiveMessage {
      case PostMessage(message) =>
        // from client, publish to others via the room
        room ! PublishSessionMessage(screenName, message)
        Behaviors.same
      case NotifyClient(message) =>
        // published from the room
        client ! message
        Behaviors.same
    }
}

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

object ChattoBotto {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      context.spawn(ChatRoom(), "chatroom")
      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
}


object Client extends App {
  import scala.concurrent.Future
  import akka.actor.ActorSystem
  import akka.stream.{ActorMaterializer, IOResult, Materializer}
  import akka.stream.scaladsl.{Sink, Source, StreamConverters}
  import akka.util.ByteString

  implicit val sys: ActorSystem = ActorSystem("ChatRoomDemo")
  implicit val mat: Materializer = ActorMaterializer()

  val stdinSource: Source[ByteString, Future[IOResult]] = StreamConverters.fromInputStream(() => System.in)
  val stdoutSink: Sink[ByteString, Future[IOResult]] = StreamConverters.fromOutputStream(() => System.out)

  def sendToChattoBotto(byteString: ByteString): ByteString =
    ByteString(byteString.utf8String)

  stdinSource.map(sendToChattoBotto).runWith(stdoutSink)
}

object Server extends App {
  val system = ActorSystem(ChattoBotto(), "ChatRoomDemo")
  val cluster = Cluster(system)
}
