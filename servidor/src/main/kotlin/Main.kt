import Classes.Jogo
import Classes.Mensagem
import Classes.ObjectUtil
import Classes.Sala
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*

var count = 0
val addresses: MutableSet<SocketAddress> = mutableSetOf() // Usando um conjunto para evitar duplicatas
var salas: MutableList<Sala> = mutableListOf();
fun main(args: Array<String>) {

    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val server = aSocket(selectorManager)
            .udp()
            .bind(InetSocketAddress("192.168.1.27", 9002))
        println("Server is listening at ${server.localAddress}")

        server.use {
            val objUtil: ObjectUtil = ObjectUtil()

            while (true) {
                try {
                    val datagram = server.receive()
                    addresses.add(datagram.address)

                    val packetSize = datagram.packet.readInt()
                    val receivedBytes = ByteArray(packetSize)
                    datagram.packet.readFully(receivedBytes)

                    val mensagem = objUtil.fromBytes(receivedBytes)
                    if(mensagem.getCriarSala()){
                        criarSala()
                    }
                    val nmensagem = Mensagem()
                    nmensagem.setSalas(salas);


                    val mBytes = objUtil.toBytes(nmensagem)

                    for (x in addresses) {
                        launch(Dispatchers.IO) {
                            try {
                                val builder = BytePacketBuilder()
                                builder.writeInt(mBytes.size)
                                builder.writeFully(mBytes)

                                server.send(Datagram(builder.build(), x))
                                println("Sent packet to: $x")
                                println(salas)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                count++
            }
        }
    }
}

fun criarSala(){
    val sala: Sala = Sala(mutableSetOf(), Jogo())
    salas.add(sala)
}