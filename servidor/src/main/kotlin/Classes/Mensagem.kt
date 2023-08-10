package Classes

import java.io.Serializable

class Mensagem : Serializable {
    private var criarSala: Boolean;
    private var salas: MutableList<Sala>;

    constructor( criarSala: Boolean? = false, salas: MutableList<Sala> = mutableListOf()) {
        this.criarSala = criarSala!!
        this.salas = salas;
    }

    public fun getCriarSala(): Boolean {
        return this.criarSala
    }

    public fun getSalas(): MutableList<Sala>{
        return this.salas
    }
    public fun setSalas(salas: MutableList<Sala>){
        this.salas = salas
    }

    public fun setCriarSala(criarSala: Boolean?){
        this.criarSala = criarSala!!
    }

}