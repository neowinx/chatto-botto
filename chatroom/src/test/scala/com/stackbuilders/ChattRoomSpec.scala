package com.stackbuilders

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.{ActorRef, Behavior}
import org.scalatest.wordspec.AnyWordSpecLike
import com.stackbuilders.ChatRoom.{GetSession, PostMessage, SessionCommand, SessionEvent, SessionGranted}

class ChattRoomSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A ChatRoom" must {
    "reply SessionGranted on new session" in {
      val replyProbe = createTestProbe[SessionEvent]()
      val underTest = spawn(ChatRoom())
      underTest ! GetSession("Santa", replyProbe.ref)
      replyProbe.receiveMessage() shouldBe a [SessionGranted]
    }
  }

}