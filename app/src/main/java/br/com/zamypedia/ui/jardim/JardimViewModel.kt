// Arquivo: app/src/main/java/br/com/zamypedia/ui/jardim/JardimViewModel.kt
package br.com.zamypedia.ui.jardim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamypedia.dados.JardimRepositorio
import br.com.zamypedia.dados.local.PlantaJardim
import br.com.zamypedia.dados.local.Vinculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.Collator
import java.util.Locale

/** As três formas de ordenar o Meu Jardim pedidas no documento de requisitos. */
enum class Ordenacao(val rotulo: String) {
    ALFABETICA("Ordem alfabética"),
    MAIS_ANTIGAS("Mais antigas primeiro"),
    MAIS_RECENTES("Mais recentes primeiro"),
}

/** Controla a tela Meu Jardim: lista de plantas, filtro por vínculo e ordenação. */
class JardimViewModel(repositorio: JardimRepositorio, donoId: String) : ViewModel() {

    /** null significa "Todas". */
    val filtro = MutableStateFlow<Vinculo?>(null)
    val ordenacao = MutableStateFlow(Ordenacao.MAIS_RECENTES)

    // Compara textos do jeito do português: "árvore" fica junto de "arruda", sem diferenciar maiúsculas
    private val comparadorPtBr = Collator.getInstance(Locale("pt", "BR")).apply { strength = Collator.PRIMARY }

    /** Lista pronta para a tela, já filtrada e ordenada. Atualiza sozinha quando algo muda. */
    val plantas: StateFlow<List<PlantaJardim>> =
        combine(repositorio.plantasDoDono(donoId), filtro, ordenacao) { lista, vinculo, ordem ->
            val filtradas = if (vinculo == null) lista else lista.filter { it.vinculoEnum == vinculo }
            when (ordem) {
                Ordenacao.ALFABETICA -> filtradas.sortedWith { a, b -> comparadorPtBr.compare(a.nomePopular, b.nomePopular) }
                Ordenacao.MAIS_ANTIGAS -> filtradas.sortedBy { it.criadoEm }
                Ordenacao.MAIS_RECENTES -> filtradas.sortedByDescending { it.criadoEm }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
