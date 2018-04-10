package $package$.webhooks.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import $package$.webhooks.api.WebhooksService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class WebhooksLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new WebhooksApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new WebhooksApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[WebhooksService])
}

abstract class WebhooksApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[WebhooksService](wire[WebhooksServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = WebhooksSerializerRegistry

  // Register the UserConversationEntity
  persistentEntityRegistry.register(wire[UserConversationEntity])
}
