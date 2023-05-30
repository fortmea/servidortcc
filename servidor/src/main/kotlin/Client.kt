import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

fun main() = runBlocking {
    val selectorManager = ActorSelectorManager(Dispatchers.IO)
    val serverAddress: SocketAddress = io.ktor.network.sockets.InetSocketAddress("127.0.0.1",9002)
    val client = aSocket(selectorManager)
        .udp()
        .connect(serverAddress)

    val message = "Olá, Xerequinha!"
    val packet = buildPacket { writeText(message) }

    client.send(Datagram(packet, serverAddress))

    val responsePacket = client.receive()
    val receivedMessage = responsePacket.packet.readText()

    println("Resposta do servidor: $receivedMessage")

    client.close()
}
