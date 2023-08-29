import Classes.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import java.util.UUID

var count = 0
val addresses: MutableMap<UUID, SocketAddress> = mutableMapOf() // Usando um conjunto para evitar duplicatas
var salas: MutableMap<Int, Sala> = mutableMapOf();
var usuarios: MutableSet<Usuario> = mutableSetOf()
fun main(args: Array<String>) {
    velha()
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val server = aSocket(selectorManager)
            .udp()
            .bind(InetSocketAddress("192.168.1.24", 9002))
        println("Server is listening at ${server.localAddress}")

        server.use {
            val objUtil = ObjectUtil()

            while (true) {
                try {
                    val datagram = server.receive()
                    val packetSize = datagram.packet.readInt()
                    val receivedBytes = ByteArray(packetSize)
                    datagram.packet.readFully(receivedBytes)
                    val nmensagem = Mensagem()
                    val mensagem = objUtil.fromBytes(receivedBytes)
                    debugMessage(mensagem)
                    if (mensagem.getUsuario() == null) {
                        val nusuario = Usuario()
                        addresses[nusuario.getId()] = datagram.address
                        nmensagem.setUsuario(nusuario)
                        usuarios.add(nusuario)
                        println(nmensagem.getUsuario().toString())
                    }

                    if (mensagem.getCriarSala()) {
                        criarSala()
                    } else if (mensagem.getEntrar()) {
                        val pos = salas[mensagem.getIdSala()]
                        if (pos!!.getUsuarios().size > 2) {
                            nmensagem.setEntrar(false)
                        } else {
                            val add: MutableSet<Usuario> = mutableSetOf()
                            add.addAll(pos.getUsuarios())
                            add.add(mensagem.getUsuario()!!)
                            pos.setUsuarios(add)
                            for (usuario in pos.getUsuarios()) {
                                pos.getPlacar()[usuario.getId()]=0
                            }
                            println("Client conectando a sala:" + mensagem.getIdSala())
                            nmensagem.setIdSala(mensagem.getIdSala()!!)
                            mensagem.setSala(pos)
                            println(pos.toString())
                            nmensagem.setEntrar(true)
                        }
                    } else if (mensagem.getMovimento()) {
                        val pos = salas[mensagem.getIdSala()]
                        pos!!.getJogo().setPosicoes(mensagem.getSala()!!.getJogo().getPosicoes()!!)
                        if (pos.getJogo().checkGameStatus() != -1) {
                            pos.setResetarTabuleiro(true)
                            if (pos.getJogo().checkGameStatus() == 1) {
                                val nplacar: MutableMap<UUID, Int> = pos.getPlacar();
                                val placaratual =
                                    if (pos.getPlacar()[pos.getSimbolo()[1]] == null) 0 else pos.getPlacar()[pos.getSimbolo()[1]]
                                nplacar[pos.getSimbolo()[1]!!] = placaratual!!+1
                                pos.setPlacar(nplacar)
                            } else if (pos.getJogo().checkGameStatus() == 2) {
                                val nplacar: MutableMap<UUID, Int> = pos.getPlacar();
                                val placaratual =
                                    if (pos.getPlacar()[pos.getSimbolo()[2]] == null) 0 else pos.getPlacar()[pos.getSimbolo()[2]]
                                nplacar[pos.getSimbolo()[2]!!] = placaratual!! + 1
                                pos.setPlacar(nplacar)
                            }
                        }
                    } else if (mensagem.getSair()) {
                        val pos = salas[mensagem.getIdSala()]
                        pos!!.getUsuarios().remove(mensagem.getUsuario()!!)
                        pos.setResetarTabuleiro(true)
                    }
                    val tAddresses: MutableSet<SocketAddress> = mutableSetOf();
                    nmensagem.setSalas(salas);
                    val mBytes = objUtil.toBytes(nmensagem)
                    if (mensagem.getSala() != null) {
                        for (x in mensagem.getSala()!!.getUsuarios()) {
                            tAddresses.add(addresses[x.getId()]!!)
                        }
                    } else {
                        tAddresses.addAll(addresses.values);
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
    val sala = Sala(mutableSetOf(), Jogo())
    salas[salas.size] = sala
}

fun debugMessage(mensagem: Mensagem) {
    println("IDSALA: " + mensagem.getIdSala())
    println("SALA OBJ: " + mensagem.getSala().toString())
    println("CRIARSALA: " + mensagem.getCriarSala())
    println("ÉMOVIMENTO: " + mensagem.getMovimento())
    println("ENTRAR: " + mensagem.getEntrar())
    println("USUARIO OBJ: " + mensagem.getUsuario().toString())
    println("SAIR: " + mensagem.getSair())
}

fun velha() {
    val board = mutableMapOf(
        "A1" to 1, "A2" to 2, "A3" to 1,
        "B1" to 2, "B2" to 2, "B3" to 2,
        "C1" to 1, "C2" to 2, "C3" to 1
    )
    var jogo = Jogo();
    jogo.setPosicoes(board)
    val result = jogo.checkGameStatus()
    println("Status do jogo: $result")
}