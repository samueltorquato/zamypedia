// Arquivo: app/src/main/java/br/com/zamypedia/ui/jardim/EdicaoPlantaViewModel.kt
package br.com.zamypedia.ui.jardim

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamypedia.dados.JardimRepositorio
import br.com.zamypedia.dados.local.Vinculo
import br.com.zamypedia.util.Fotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controla a tela Adicionar planta, que também serve para editar.
 * Quando [idPlanta] vem preenchido, os campos começam com os dados da planta existente.
 */
class EdicaoPlantaViewModel(
    private val repositorio: JardimRepositorio,
    private val donoId: String,
    private val idPlanta: String?,
) : ViewModel() {

    var nomePopular by mutableStateOf("")
    var nomeCientifico by mutableStateOf("")
    var vinculo by mutableStateOf(Vinculo.TENHO)
    var fotoCaminho by mutableStateOf<String?>(null)
        private set
    var processandoFoto by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set

    val editando: Boolean get() = idPlanta != null

    // Fotos novas que ainda não foram salvas. Se a pessoa desistir, elas são apagadas.
    private val fotosNaoSalvas = mutableListOf<String>()
    private var salvou = false

    init {
        if (idPlanta != null) {
            viewModelScope.launch {
                repositorio.buscar(idPlanta)?.let {
                    nomePopular = it.nomePopular
                    nomeCientifico = it.nomeCientifico ?: ""
                    vinculo = it.vinculoEnum
                    fotoCaminho = it.fotoCaminho
                }
            }
        }
    }

    /** Recebe a foto escolhida (câmera ou galeria) e comprime fora da tela principal. */
    fun aoEscolherFoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            processandoFoto = true
            try {
                val caminho = withContext(Dispatchers.IO) { Fotos.comprimirESalvar(context.applicationContext, uri) }
                // Se já havia uma foto nova não salva, ela é descartada
                fotoCaminho?.takeIf { it in fotosNaoSalvas }?.let { Fotos.apagar(it) }
                fotosNaoSalvas += caminho
                fotoCaminho = caminho
            } catch (e: Exception) {
                mensagem = "Não foi possível usar esta foto."
            } finally {
                processandoFoto = false
            }
        }
    }

    fun removerFoto() {
        fotoCaminho?.takeIf { it in fotosNaoSalvas }?.let { Fotos.apagar(it) }
        fotoCaminho = null
    }

    fun salvar(aoSalvar: () -> Unit) {
        if (nomePopular.isBlank()) {
            mensagem = "Digite o nome popular da planta."
            return
        }
        viewModelScope.launch {
            repositorio.salvar(idPlanta, donoId, nomePopular, nomeCientifico, vinculo, fotoCaminho)
            salvou = true
            aoSalvar()
        }
    }

    override fun onCleared() {
        // Saiu sem salvar: apaga as fotos comprimidas que não serão usadas
        if (!salvou) fotosNaoSalvas.forEach { Fotos.apagar(it) }
        else fotosNaoSalvas.filter { it != fotoCaminho }.forEach { Fotos.apagar(it) }
    }
}
