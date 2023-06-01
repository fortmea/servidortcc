package Classes

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ObjectUtil {
    fun toBytes(entity: Any): ByteArray {
        var baos = ByteArrayOutputStream()
        var oos = ObjectOutputStream(baos)
        oos.writeObject(entity)
        var bufferClass = baos.toByteArray()
        return bufferClass;
    }
    fun fromBytes(byteArray: ByteArray): Mensagem {
        val bufferResposta: ByteArray = byteArray
        val bais = ByteArrayInputStream(bufferResposta)
        val ois = ObjectInputStream(bais)
        val resposta: Mensagem = ois.readObject() as Mensagem
        return resposta;
    }
}