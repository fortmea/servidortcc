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
var salas: MutableMap<Int, Sala> = mutableMapOf();
fun main(args: Array<String>) {
    velha()
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val server = aSocket(selectorManager)
            .udp()
            .bind(InetSocketAddress("192.168.1.25", 9002))
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
                    val nmensagem = Mensagem()
                    val mensagem = objUtil.fromBytes(receivedBytes)
                    if (mensagem.getCriarSala()) {
                        criarSala()
                    } else if (mensagem.getEntrar()) {
                        val pos = salas[mensagem.getIdSala()]
                        if (pos!!.getAddresses().size>2){
                            nmensagem.setEntrar(false)
                        }else{
                            val add:  MutableSet<SocketAddress> = mutableSetOf()
                            add.addAll(pos.getAddresses())
                            add.add(datagram.address)
                            pos.setAddresses(add)
                            println("Client conectando a sala:" + mensagem.getIdSala())
                            nmensagem.setIdSala(mensagem.getIdSala()!!)
                            nmensagem.setEntrar(true)
                        }
                    } else if (mensagem.getMovimento()) {
                        val pos = salas[mensagem.getIdSala()]
                        pos!!.getJogo().setPosicoes(mensagem.getSala()!!.getJogo().getPosicoes()!!)
                    }
                    val nsalas: MutableMap<Int, Sala> = mutableMapOf()
                    nsalas.putAll(salas)
                    var tAddresses: MutableSet<SocketAddress> = mutableSetOf();
                    nsalas.forEach {
                        it.value.setJogadores(it.value.getAddresses().size)
                        it.value.setAddresses(mutableSetOf())
                    }
                    nmensagem.setSalas(nsalas);
                    val mBytes = objUtil.toBytes(nmensagem)
                    if (mensagem.getSala() != null) {
                        tAddresses = mensagem.getSala()!!.getAddresses()
                    } else {
                        tAddresses.addAll(addresses);
                    }
                    for (x in tAddresses) {
                        launch(Dispatchers.IO) {
                            try {
                                val builder = BytePacketBuilder()
                                builder.writeInt(mBytes.size)
                                builder.writeFully(mBytes)

                                server.send(Datagram(builder.build(), x))

                                println(salas)
                                println("Sent packet to: $x")
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

fun criarSala() {
    val sala: Sala = Sala(mutableSetOf(), Jogo())
    salas[salas.size] = sala
}

fun velha(){
    val board = mutableMapOf(
        "A1" to 1, "A2" to 2, "A3" to 1,
        "B1" to 2, "B2" to 1, "B3" to 2,
        "C1" to 1, "C2" to 2, "C3" to 1
    )
    var jogo = Jogo();
    jogo.setPosicoes(board)
    val result = jogo.checkGameStatus()
    println("Status do jogo: $result")
}