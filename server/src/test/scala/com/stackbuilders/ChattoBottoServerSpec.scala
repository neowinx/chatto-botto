package com.stackbuilders

import akka.actor.testkit.typed.Effect.Spawned
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.stackbuilders.ChatRoom.{RoomCommand, SessionEvent, SessionGranted}
import org.scalatest.wordspec.AnyWordSpecLike

class ChattoBottoServerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "A ChattoBottoServer" must {
    "spawn a ChatRoom" in {
      spawn(ChattoBottoServer())
    }
  }

  "A ChattRoom" must {
    "spawn a" in {
      val respondTo = createTestProbe[SessionEvent]()
      val underTest = spawn(ChatRoom())
      //underTest ! ChatRoom.GetSession("maria", respondTo)
      //respondTo.expectMessage(SessionGranted(handle = ActorRe))
    }
  }

}