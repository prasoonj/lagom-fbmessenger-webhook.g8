package $package$.webhooks.api

import play.api.libs.json.{Format, Json}

object FacebookWebhookEvents {
/**
 * Every FacebookWebhookEvent has this common structure:
 *
 *
 * {
 *   "object":"page",
 *    "entry":[
 *      {
 *        "id":"<PAGE_ID>",
 *        "time":1458692752478,
 *        "messaging":[
 *          {
 *            "sender":{
 *              "id":"<PSID>"
 *            },
 *            "recipient":{
 *              "id":"<PAGE_ID>"
 *            },
 *
 *            ...
 *          }
 *        ]
 *      }
 *    ]
 *  }
 */

  case class Payload(url: String)
  object Payload {
    implicit val format: Format[Payload] = Json.format
  }

  case class QuickReply(payload: String)
  object QuickReply {
    implicit val format: Format[QuickReply] = Json.format
  }

  case class Attachment(`type`: String,
      payload: Payload,
      title: String,
      URL: String)

  object Attachment {
    implicit val format: Format[Attachment] = Json.format
  }

  case class Sender(id: String)
  object Sender {
    implicit val format: Format[Sender] = Json.format
  }
  case class Recipient(id: String)
  object Recipient {
    implicit val format: Format[Recipient] = Json.format
  }

  //TODO: Use Option on text, quick_reply and attachment to determine the type of message
  // and have it in the Message Object
  case class Message(mid: String,
      text: String,
      quick_reply: QuickReply,
      seq: Int,
      attachments: Seq[Attachment])
  object Message {
    implicit val format: Format[Message] = Json.format
    val Empty = Message("", "", QuickReply(""), 0, Nil)
  }

  case class Messaging(sender: Sender, recipient: Recipient, message: Message)
  object Messaging {
    implicit val format: Format[Messaging] = Json.format
  }

  // There can be one or more EventEntries
  // TODO: format for timestamp
  case class EventEntry(id: String, messaging: Seq[Messaging])
  object EventEntry {
    implicit val format: Format[EventEntry] = Json.format
  }

  // Message Received Events
  case class UserReplyMessage(`object`: String, entry: Seq[EventEntry])
  object UserReplyMessage {
    implicit val format: Format[UserReplyMessage] = Json.format
  }
}
