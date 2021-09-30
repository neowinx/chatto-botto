package com.stackbuilders

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import com.stackbuilders.ChatRoom.GetSession

class ChattRoomSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A ChatRoom" must {
    "reply to client on new session" in {
      val replyProbe = createTestProbe[ChattoBotto]()
      val underTest = spawn(ChatRoom())
      underTest ! GetSession("Santa", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Santa", underTest.ref))
    }
  }

}