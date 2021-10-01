package com.stackbuilders

import akka.actor.testkit.typed.Effect.Spawned
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.stackbuilders.ChatRoom.{GetSession, PostMessage, RoomCommand, SessionCommand, SessionEvent, SessionGranted}
import org.scalatest.wordspec.AnyWordSpecLike

class ChattoBottoServerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "A ChattoBottoServer" must {
    "spawn a ChatRoom" in {
      val underTest = spawn(ChattoBottoServer())
      //TODO: See how we can check for the child actors on tests
    }
  }

  "A ChattRoom" must {
    "grant session to clients" in {
      val chatRoomActor = spawn(ChatRoom())
      val clientProbe = createTestProbe[SessionEvent]()
      chatRoomActor ! ChatRoom.GetSession("maria", clientProbe.ref)
      val message = clientProbe.receiveMessage()
      message shouldBe a [SessionGranted]
    }
  }

}