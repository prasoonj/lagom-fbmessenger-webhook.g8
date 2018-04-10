package $package$.webhooks.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import $package$.webhooks.api._

class WebhooksServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new WebhooksApplication(ctx) with LocalServiceLocator
  }

  val webhook = server.serviceClient.implement[WebhooksService]

  override protected def afterAll() = server.stop()

  "webhook service" should {

    "say hello" in {
      webhook.verifyFacebookWebhook("CHALLENGE_ACCEPTED", "subscribe", "somecomplicatedtoken")
      .invoke().map { answer =>
        answer should ===("CHALLENGE_ACCEPTED")
      }
    }
  }
}
