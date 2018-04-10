package $package$.webhooks.impl

import akka.{Done, NotUsed}
import $package$.webhooks.api.{WebhooksService, FacebookWebhookEvents, FacebookEvent}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import java.util.UUID
import scala.concurrent.Future
import com.lightbend.lagom.scaladsl.api.transport.Forbidden

class WebhooksServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends WebhooksService {

  def verifyFacebookWebhook(`hub.challenge`: String,
      `hub.mode`: String,
      `hub.verify_token`: String) = ServerServiceCall { request =>

        //TODO: Pull this out from client configuration
        val FACEBOOK_VERIFICATION_TOKEN = "$facebookVerificationToken$"
        if(`hub.verify_token`.equals(FACEBOOK_VERIFICATION_TOKEN)) Future.successful(`hub.challenge`)
            else Future.failed(throw Forbidden("The verification token does not match"))
  }

  def parseFacebookMessage() = ServiceCall[FacebookWebhookEvents.UserReplyMessage, Done] { request =>
    // All the bundled messages would have the same senderId - get any one of them
    request.entry.map { entry =>
      entry.messaging.map { messaging =>
        val userMsg = messaging.message
        val psid = messaging.sender.id
        val ref = persistentEntityRegistry.refFor[UserConversationEntity](psid)
        ref.ask(UpdateUserMessageState(psid, userMsg))
      }
    }
    Future.successful(Done)
  }

  override def webhooksTopic(): Topic[FacebookEvent] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(WebhookEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }
  private def convertEvent(webhookEvent: EventStreamElement[WebhookEvent]): FacebookEvent = {
    webhookEvent.event match {
      case FacebookWebhookEvent(psid, message) => FacebookEvent(psid, message)
    }
  }


}
