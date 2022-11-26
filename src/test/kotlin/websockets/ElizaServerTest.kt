@file:Suppress("NoWildcardImports")
package websockets

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.lang.Nullable
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.util.AssertionErrors.assertEquals
import org.springframework.test.util.AssertionErrors.assertTrue
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.lang.reflect.Type
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.websocket.*


/*
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SpringRabbitTest
class ElizaServerTest {

    private lateinit var container: WebSocketContainer

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        container = ContainerProvider.getWebSocketContainer()
    }

    @Test
    fun onOpen() {
        val latch = CountDownLatch(3)
        val list = mutableListOf<String>()

        val client = ElizaOnOpenMessageHandler(list, latch)
        container.connectToServer(client, URI("ws://localhost:$port/eliza"))
        latch.await()
        assertEquals(3, list.size)
        assertEquals("The doctor is in.", list[0])
    }

    @Test
    fun onChat() {
        val latch = CountDownLatch(4)
        val list = mutableListOf<String>()

        val client = ElizaOnOpenMessageHandlerToComplete(list, latch)
        container.connectToServer(client, URI("ws://localhost:$port/eliza"))
        latch.await()
        assertTrue(list.size > 3)
        assertEquals("You don't seem very certain.", list[3])
    }
}

@ClientEndpoint
class ElizaOnOpenMessageHandler(private val list: MutableList<String>, private val latch: CountDownLatch) {
    @OnMessage
    fun onMessage(message: String) {
        list.add(message)
        latch.countDown()
    }
}

@ClientEndpoint
class ElizaOnOpenMessageHandlerToComplete(private val list: MutableList<String>, private val latch: CountDownLatch) {

    @OnMessage
    fun onMessage(message: String, session: Session) {
        list.add(message)
        latch.countDown()
        if (list.size == 3) {
            session.basicRemote.sendText("maybe")
        }
    }
}
*/

const val ELIZA_STOMP_ENDPOINT = "gs-guide-websocket"
const val ELIZA_ENDPOINT = "/app/eliza"
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ElizaServerTestStomp {

    @LocalServerPort
    private val port: Int = 0

    private lateinit var webSocketStompClient: WebSocketStompClient

    @BeforeEach
    fun setup() {
        webSocketStompClient = WebSocketStompClient(
            SockJsClient(
                listOf<Transport>(WebSocketTransport(StandardWebSocketClient()))
            )
        )

        webSocketStompClient.messageConverter = StringMessageConverter()
    }

    @Test
    fun onChat() {
        val latch = CountDownLatch(2)
        val list = mutableListOf<String>()

        val session: StompSession = webSocketStompClient
            .connect("ws://localhost:$port/$ELIZA_STOMP_ENDPOINT", object : StompSessionHandlerAdapter() {})[1, TimeUnit.SECONDS]

        session.subscribe("/topic/responses", object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders): Type {
                return String::class.java
            }

            override fun handleFrame(headers: StompHeaders, @Nullable payload: Any?) {
                list.add(payload as? String ?: throw Exception("No payload!"))
            }
        })

        session.send(ELIZA_ENDPOINT, "maybe");
        latch.await()
        assertTrue("list should have 2 messages", list.size > 1)
        assertEquals("Messages should be equal","You don't seem very certain.", list[0])
    }
}
