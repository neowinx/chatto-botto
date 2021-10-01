package com.stackbuilders


import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors

import java.net.URLEncoder
import java.nio.charset.StandardCharsets


object ChatRoom {
  sealed trait RoomCommand
  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends RoomCommand
  final case class Disconnect(screenName: String) extends RoomCommand

  sealed trait SessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  final case class MessagePosted(screenName: String, message: String) extends SessionEvent
  final case class Post(message: String) extends SessionEvent

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
            parseName(screenName))
          client ! SessionGranted(ses)
          chatRoom(ses :: sessions)
        case Disconnect(screenName) =>
          val name = parseName(screenName)
          context.child(name) match {
            case Some(ses: ActorRef[SessionCommand]) =>
              context.stop(ses)
              chatRoom(sessions.filterNot(s => s == ses))
            case _ =>
              Behaviors.same
          }
        case PublishSessionMessage(screenName, message) =>
          val notification = NotifyClient(MessagePosted(screenName, message))
          sessions.foreach(_ ! notification)
          Behaviors.same
      }
    }

  private def parseName(screenName: String) =
    URLEncoder.encode(screenName, StandardCharsets.UTF_8.name)

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

