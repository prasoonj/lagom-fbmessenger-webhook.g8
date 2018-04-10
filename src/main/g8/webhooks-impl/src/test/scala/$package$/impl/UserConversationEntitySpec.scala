package $package$.webhooks.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver

import $package$.webhooks.api.FacebookWebhookEvents

import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import $package$.webhooks.api.FacebookWebhookEvents.QuickReply
import akka.Done

class UserConversationEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("UserConversationEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(WebhooksSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[UserConversationEntityCommand[_],
    WebhookEvent,
    UserConversationState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new UserConversationEntity, "")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "UserConversation entity" should {

    "say hello by default" in withTestDriver { driver =>
      val psid = "asdf"
      val message: FacebookWebhookEvents.Message = FacebookWebhookEvents.Message("mid.1457764197618:41d102a3e1ae206a38",
          "Test Message",
          QuickReply(""),
          0,
          Nil)
      val outcome = driver.run(UpdateUserMessageState(psid, message))
      outcome.replies shouldBe Vector(Done)
    }

  }
}
