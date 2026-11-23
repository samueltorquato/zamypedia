// Arquivo: app/src/main/java/br/com/zamypedia/ui/jardim/FichaViewModel.kt
package br.com.zamypedia.ui.jardim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamypedia.dados.JardimRepositorio
import br.com.zamypedia.dados.RegistrosRepositorio
import br.com.zamypedia.dados.local.Floracao
import br.com.zamypedia.dados.local.PlantaJardim
import br.com.zamypedia.dados.local.Registro
import br.com.zamypedia.dados.local.TipoRegistro
import br.com.zamypedia.lembretes.CalculoLembretes
import br.com.zamypedia.lembretes.Previsao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Os textos do Bloco 01, na mesma ordem dos campos da tela. */
data class InformacoesPlanta(
    val taxonomia: String = "",
    val luz: String = "",
    val agua: String = "",
    val solo: String = "",
    val nutrientes: String = "",
    val sinaisFolhas: String = "",
    val sinaisPragas: String = "",
) {
    companion object {
        fun de(p: PlantaJardim) = InformacoesPlanta(
            p.taxonomia.orEmpty(), p.luz.orEmpty(), p.agua.orEmpty(), p.solo.orEmpty(),
            p.nutrientes.orEmpty(), p.sinaisFolhas.orEmpty(), p.sinaisPragas.orEmpty(),
        )
    }
}

/** Controla a ficha da planta: dados da planta, registros e próximos cuidados. */
class FichaViewModel(
    private val jardim: JardimRepositorio,
    private val registrosRepo: RegistrosRepositorio,
    private val plantaId: String,
) : ViewModel() {

    val planta: StateFlow<PlantaJardim?> = jardim.planta(plantaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val registros: StateFlow<List<Registro>> = registrosRepo.daPlanta(plantaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Próximos cuidados desta planta, do mais urgente para o mais distante. */
    val previsoes: StateFlow<List<Previsao>> = registrosRepo.daPlanta(plantaId)
        .map { lista -> CalculoLembretes.previsoes(lista).sortedBy { it.proxima } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Grava o Bloco 01. Campo vazio vira "sem informação" (null). */
    fun salvarInformacoes(info: InformacoesPlanta) = viewModelScope.launch {
        fun limpo(texto: String) = texto.trim().ifBlank { null }
        jardim.alterar(plantaId) {
            it.copy(
                taxonomia = limpo(info.taxonomia),
                luz = limpo(info.luz),
                agua = limpo(info.agua),
                solo = limpo(info.solo),
                nutrientes = limpo(info.nutrientes),
                sinaisFolhas = limpo(info.sinaisFolhas),
                sinaisPragas = limpo(info.sinaisPragas),
            )
        }
    }

    /** Grava "desde quando está em casa". Ano null apaga a informação. */
    fun salvarDesde(ano: Int?, mes: Int?, dia: Int?) = viewModelScope.launch {
        jardim.alterar(plantaId) { it.copy(desdeAno = ano, desdeMes = mes, desdeDia = dia) }
    }

    fun salvarFloracao(floracao: Floracao?) = viewModelScope.launch {
        jardim.alterar(plantaId) { it.copy(floracao = floracao?.name) }
    }

    fun adicionarRegistro(tipo: TipoRegistro, data: LocalDate) = viewModelScope.launch {
        registrosRepo.registrar(plantaId, tipo, data)
        // Anotar uma floração deixa a situação como "já floresceu", se ainda não estava marcada
        if (tipo == TipoRegistro.FLORACAO) {
            jardim.alterar(plantaId) {
                if (it.floracao == null || it.floracao == Floracao.NUNCA.name) it.copy(floracao = Floracao.JA_FLORESCEU.name) else it
            }
        }
    }

    fun apagarRegistro(id: String) = viewModelScope.launch { registrosRepo.apagar(id) }
}
