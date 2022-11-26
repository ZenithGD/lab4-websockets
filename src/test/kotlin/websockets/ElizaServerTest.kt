@file:Suppress("NoWildcardImports")
package websockets

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*
import org.springframework.stereotype.Component
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

const val ELIZA_STOMP_ENDPOINT = "/app/eliza"
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ElizaServerTestStomp {
    private lateinit var rabbitTemplateMock: RabbitTemplate
    private lateinit var subject: MessageSender

    @BeforeEach
    fun setUp() {
        rabbitTemplateMock = Mockito.mock(RabbitTemplate::class.java)
        subject = MessageSender(rabbitTemplateMock)
    }
}

@Component
class MessageSender(private val rabbitTemplate: RabbitTemplate) {
    fun send(message: String) {
        rabbitTemplate.convertAndSend(ELIZA_STOMP_ENDPOINT, message)
    }

}