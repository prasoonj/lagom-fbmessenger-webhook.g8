package $package$.webhooks.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import scalaz._
import $package$.webhooks.api.FacebookWebhookEvents.Message

object WebhooksService  {
  val TOPIC_NAME = "webhooks-registry"
}

trait WebhooksService extends Service {

  def verifyFacebookWebhook(`hub.challenge`: String,
      `hub.mode`: String,
      `hub.verify_token`: String): ServiceCall[NotUsed, String]

  def parseFacebookMessage(): ServiceCall[FacebookWebhookEvents.UserReplyMessage, Done] //TODO: Use Either monad here

  def webhooksTopic(): Topic[FacebookEvent]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("webhooks")
      .withCalls(
        pathCall("/api/v1/fbwebhook", parseFacebookMessage _),
        pathCall("/api/v1/fbwebhook?hub.challenge&hub.mode&hub.verify_token", verifyFacebookWebhook _)
      )
      .withTopics(
        topic(WebhooksService.TOPIC_NAME, webhooksTopic)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // accountId as the partition key.
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[FacebookEvent](_.senderId)
          )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}


case class FbWebhookMessage(nodeId: String)
object FbWebhookMessage {
  implicit val format: Format[FbWebhookMessage] = Json.format[FbWebhookMessage]
}

// senderId is the PSID of the user. Using it as a partition key ensures that messages
// are delivered in order per user. The number of users per page is a good metric to
// decide pricing slabs as well!
case class FacebookEvent(senderId: String, message: Message)
object FacebookEvent {
  implicit val format: Format[FacebookEvent] = Json.format
}
