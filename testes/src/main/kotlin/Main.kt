import br.dev.tech.teste.classes.Mensagem
import classes.ObjectUtil
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.PortUnreachableException

fun main() {
    println("Selecione o tipo de servidor:\n1 - UDP\n2 - TCP")
    val input = readln()
    if (input == "1") {
        println("Executando servidor UDP")
        udp()
    } else if (input == "2") {
        println("Executando servidor TCP")
        tcp()
    }

}

fun udp() {
    runBlocking {

        val selectorManager = SelectorManager(Dispatchers.IO)
        val server = aSocket(selectorManager).udp().bind(InetSocketAddress("192.168.1.25", 9002))
        println("Server is listening at ${server.localAddress}")
        server.use {
            val objUtil = ObjectUtil()
            while (true) {
                try {
                    val datagram = server.receive()
                    //println("RECEBENDO DADOS")
                    val packetSize = datagram.packet.readInt()
                    val receivedBytes = ByteArray(packetSize)
                    datagram.packet.readFully(receivedBytes)
                    val mensagem = objUtil.fromBytes(receivedBytes)
                    var nmensagem: Mensagem
                    if (mensagem.mensagem == "") {
                        nmensagem = Mensagem("Qual seu nome?")
                    } else {
                        nmensagem = Mensagem("Olá, ${mensagem.mensagem}")
                    }
                    launch(Dispatchers.IO) {
                        var mBytes = objUtil.toBytes(nmensagem)
                        try {
                            val builder = BytePacketBuilder()
                            builder.writeInt(mBytes.size)
                            builder.writeFully(mBytes)
                            server.send(Datagram(builder.build(), datagram.address))
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

}

fun tcp() {
    runBlocking {

        val selectorManager = SelectorManager(Dispatchers.IO)
        val server = aSocket(selectorManager).tcp().bind(InetSocketAddress("192.168.1.25", 9002))

        println("Server is listening at ${server.localAddress}")

        val objUtil = ObjectUtil()
        while (true) {
            val socket = server.accept()
            println("OK?")
            launch {
                var receiveChannel = socket.openReadChannel()
                var sendChannel = socket.openWriteChannel()
                try {
                    while (true) {
                        val byteReadPacket = receiveChannel.readPacket(receiveChannel.readInt())
                        //println("RECEBENDO DADOS")
                        val mensagem = objUtil.fromBytes(byteReadPacket.readBytes())
                        var nmensagem: Mensagem
                        //println(mensagem.mensagem)
                        if (mensagem.mensagem == "") {
                            nmensagem = Mensagem("Qual seu nome?")
                        } else {
                            nmensagem = Mensagem("Olá, ${mensagem.mensagem}")
                        }
                        withContext(Dispatchers.IO) {
                            try {
                                val mBytes = objUtil.toBytes(nmensagem)
                                val packet = buildPacket {
                                    writeInt(mBytes.size) // Grava o tamanho dos bytes
                                    writeFully(mBytes)    // Grava os bytes da mensagem
                                }
                                sendChannel.writePacket(packet)
                                sendChannel.flush()

                                //println("DEVERIA TER ENVIADO")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

}

