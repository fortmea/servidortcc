import Classes.Jogo
import Classes.Mensagem
import Classes.ObjectUtil
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer

fun main() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverAddress: SocketAddress = InetSocketAddress("127.0.0.1", 9002)
        val client = aSocket(selectorManager)
            .udp()
            .connect(serverAddress)
        launch(Dispatchers.IO) {
                println("Ok!")
                var posicoes: MutableMap<String,Int> = hashMapOf();
                posicoes["A1"]=0;
                var jogo: Jogo = Jogo();
                jogo.setPosicoes(posicoes)
                var objUtil: ObjectUtil = ObjectUtil();
                var mensagem = Mensagem();
                val mBytes = objUtil.toBytes(mensagem);
                val packet = buildPacket {
                    writeByteBufferDirect(mBytes.size) { buffer -> buffer.put(mBytes) }

                }
                client.send(Datagram(packet, serverAddress))
                val responsePacket = client.receive()
                val receivedMessage = objUtil.fromBytes(responsePacket.packet.readBytes())

                println("Resposta do servidor: ${receivedMessage.toString()}")

            }


    }
}
