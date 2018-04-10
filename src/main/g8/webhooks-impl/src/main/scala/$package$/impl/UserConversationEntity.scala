package $package$.webhooks.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEvent, AggregateEventTag, PersistentEntity }
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{ JsonSerializer, JsonSerializerRegistry }
import play.api.libs.json.{ Format, Json }

import scala.collection.immutable.Seq
import $package$.webhooks.api
import java.util.UUID

import $package$.webhooks.api.FacebookWebhookEvents

/**
 * This entity captures the current state of the User's chat with a particular page.
 * The current state captures what the user's last message was.
 * The stream of events produced by this entity builds the user-side of the entire
 * conversation.
 */
class UserConversationEntity extends PersistentEntity {

  override type Command = UserConversationEntityCommand[_]
  override type Event = WebhookEvent
  override type State = UserConversationState

  /**
   * The initial state. This is used if there is no snapshotted state to be found - the
   * first time a user interacts with a given Page.
   */
  override def initialState: UserConversationState = UserConversationState.empty

  /**
   * An entity can define different behaviours for different states, so the behaviour
   * is a function of the current state to a set of actions.
   */
  override def behavior: Behavior = {
    case UserConversationState(intentTree) => Actions()
      .onCommand[UpdateUserMessageState, Done] {

        case (UpdateUserMessageState(psid, message), ctx, state) =>
          ctx.thenPersist(
              FacebookWebhookEvent(psid, message)) { _ =>
                    ctx.reply(Done)
                  }
      }
      .onEvent {

        case (FacebookWebhookEvent(psid, message), state) =>
          UserConversationState(message)
      }

  }
}

/**
 * The current state held by the persistent entity.
 */
case class UserConversationState(sentMessage: api.FacebookWebhookEvents.Message)

object UserConversationState {
  val empty = UserConversationState(api.FacebookWebhookEvents.Message.Empty)
  implicit val format: Format[UserConversationState] = Json.format
}

/**
 * This interface defines all the events that the UserConversationEntity supports.
 */
sealed trait WebhookEvent extends AggregateEvent[WebhookEvent] {
  def aggregateTag = WebhookEvent.Tag
}

object WebhookEvent {
  val Tag = AggregateEventTag[WebhookEvent]
}

/**
 * From Facebook's documentation:
 * Every time a person starts a conversation with your Messenger bot,
 * the Messenger Platform will assign them a page-scoped ID, commonly referred to as a PSID.
 * This ID is unique to your Facebook Page, and cannot be used with any other Facebook app or Page.
 * In other words, a person will have different PSIDs for each Messenger bot
 * they are in conversation with. This ensures that a person can only be sent messages
 * by bots they have chosen to start conversations with.
 */
case class FacebookWebhookEvent(psid: String,
    message: api.FacebookWebhookEvents.Message) extends WebhookEvent

object FacebookWebhookEvent {

  implicit val format: Format[FacebookWebhookEvent] = Json.format
}

sealed trait UserConversationEntityCommand[R] extends ReplyType[R]

case class UpdateUserMessageState(psid: String,
    message: api.FacebookWebhookEvents.Message) extends UserConversationEntityCommand[Done]
object UpdateUserMessageState {
  implicit val format: Format[UpdateUserMessageState] = Json.format
}

object WebhooksSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[FacebookWebhookEvent],
    JsonSerializer[UserConversationState],
    JsonSerializer[UpdateUserMessageState],
    JsonSerializer[api.FacebookWebhookEvents.UserReplyMessage])
}
