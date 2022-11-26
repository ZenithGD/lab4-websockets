@file:Suppress("NoWildcardImports", "WildcardImport", "SpreadOperator")
package websockets

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.web.socket.config.annotation.*
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import java.util.*
import javax.websocket.*

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    @Bean
    fun serverEndpoint() = ServerEndpointExporter()
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/gs-guide-websocket").withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setPreservePublishOrder(true)
    }
}

@Controller
class ElizaController(
    @Autowired val messagingTemplate: SimpMessagingTemplate
) {

    @EventListener(SessionSubscribeEvent::class)
    fun onSubscribeEvent(sessionSubscribeEvent: SessionSubscribeEvent) {

        val headerAccessor = StompHeaderAccessor.wrap(sessionSubscribeEvent.message)
        LOGGER.info("User with ID ${headerAccessor.sessionId} subscribed.")

        // messagingTemplate.convertAndSend( "/topic/responses", "The doctor is in." );
        // messagingTemplate.convertAndSend( "/topic/responses", "What's on your mind?" );
        // messagingTemplate.convertAndSend( "/topic/responses", "---" );
    }

    @EventListener(SessionDisconnectEvent::class)
    fun onDisconnectEvent(sessionDisconnectEvent: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(sessionDisconnectEvent.message)
        LOGGER.info("User with ID ${headerAccessor.sessionId} disconnected.")
    }

    private val eliza = Eliza()

    @MessageMapping("/eliza")
    @Throws(Exception::class)
    fun elizaRespond(@Payload message: String) {

        val currentLine = Scanner(message.lowercase(Locale.getDefault()))
        if (currentLine.findInLine("bye") == null) {
            messagingTemplate.convertAndSend("/topic/responses", eliza.respond(currentLine))
            messagingTemplate.convertAndSend("/topic/responses", "---")
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(exception: Throwable): String? {
        return exception.message
    }
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElizaController::class.java)
    }
}

/*
class ElizaEndpoint {

    private val eliza = Eliza()

    /**
     * Successful connection
     *
     * @param session
     */
    @OnOpen
    fun onOpen(session: Session) {
        LOGGER.info("Server Connected ... Session ${session.id}")
        synchronized(session) {
            with(session.basicRemote) {
                sendText("The doctor is in.")
                sendText("What's on your mind?")
                sendText("---")
            }
        }
    }

    /**
     * Connection closure
     *
     * @param session
     */
    @OnClose
    fun onClose(session: Session, closeReason: CloseReason) {
        LOGGER.info("Session ${session.id} closed because of $closeReason")
    }

    /**
     * Message received
     *
     * @param message
     */
    @OnMessage
    fun onMsg(message: String, session: Session) {
        LOGGER.info("Server Message ... Session ${session.id}")
        val currentLine = Scanner(message.lowercase(Locale.getDefault()))
        if (currentLine.findInLine("bye") == null) {
            LOGGER.info("Server received \"$message\"")
            synchronized(session) {
                with(session.basicRemote) {
                    sendText(eliza.respond(currentLine))
                    sendText("---")
                }
            }
        } else {
            session.close(CloseReason(CloseCodes.NORMAL_CLOSURE, "Alright then, goodbye!"))
        }
    }

    @OnError
    fun onError(session: Session, errorReason: Throwable) {
        LOGGER.error("Session ${session.id} closed because of ${errorReason.javaClass.name}", errorReason)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElizaEndpoint::class.java)
    }
}
*/
