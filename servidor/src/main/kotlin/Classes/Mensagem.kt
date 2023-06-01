package Classes

import java.io.Serializable

class Mensagem: Serializable {
    private val posicoes: MutableMap<String, Int>;

    constructor(posicoes: MutableMap<String, Int>) {
        this.posicoes = posicoes
    }

    public fun getPosicoes(): MutableMap<String,Int>{
        return this.posicoes;
    }

}