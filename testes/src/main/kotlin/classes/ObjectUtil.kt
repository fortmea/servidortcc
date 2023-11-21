package classes

import br.dev.tech.teste.classes.Mensagem
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ObjectUtil {

    fun toBytes(entity: Any): ByteArray {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(entity)
        oos.flush()
        return baos.toByteArray()
    }

    fun fromBytes(byteArray: ByteArray): Mensagem{
        val bais = ByteArrayInputStream(byteArray)
        val ois = ObjectInputStream(bais)
        return ois.readObject() as Mensagem
    }
}