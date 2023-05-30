import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
fun main(args: Array<String>) {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val server = aSocket(selectorManager)
            .udp()
            .bind(InetSocketAddress("127.0.0.1", 9002))
        println("Server is listening at ${server.localAddress}")
        server.use {
            val receiveChannel = server.openReadChannel()
            while (true) {
                try {
                    val datagram = server.receive()
                    val builder = BytePacketBuilder()
                    val input = datagram.packet.readUTF8Line()!!;
                    builder.writeText(input)

                    println(input)
                    server.send(Datagram(builder.build(), datagram.address))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}