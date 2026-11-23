// Arquivo: app/src/main/java/br/com/zamypedia/dados/JardimRepositorio.kt
package br.com.zamypedia.dados

import br.com.zamypedia.dados.local.PlantaJardim
import br.com.zamypedia.dados.local.PlantaJardimDao
import br.com.zamypedia.dados.local.Vinculo
import br.com.zamypedia.util.Fotos
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Único lugar que as telas usam para ler e gravar plantas do Meu Jardim.
 *
 * Nesta entrega tudo fica só no aparelho. Quando a sincronização chegar,
 * ela será acrescentada aqui, sem mudar as telas.
 */
class JardimRepositorio(private val dao: PlantaJardimDao) {

    fun plantasDoDono(donoId: String): Flow<List<PlantaJardim>> = dao.observarDoDono(donoId)

    fun planta(id: String): Flow<PlantaJardim?> = dao.observarPorId(id)

    suspend fun buscar(id: String): PlantaJardim? = dao.buscarPorId(id)

    /** Cria uma planta nova (quando [idExistente] é nulo) ou atualiza uma existente. */
    suspend fun salvar(
        idExistente: String?,
        donoId: String,
        nomePopular: String,
        nomeCientifico: String?,
        vinculo: Vinculo,
        fotoCaminho: String?,
    ) {
        val agora = System.currentTimeMillis()
        val anterior = idExistente?.let { dao.buscarPorId(it) }

        // Se a foto foi trocada, a antiga é apagada para não ocupar espaço
        if (anterior?.fotoCaminho != null && anterior.fotoCaminho != fotoCaminho) {
            Fotos.apagar(anterior.fotoCaminho)
        }

        dao.salvar(
            PlantaJardim(
                id = anterior?.id ?: UUID.randomUUID().toString(),
                donoId = donoId,
                nomePopular = nomePopular.trim(),
                nomeCientifico = nomeCientifico?.trim()?.ifBlank { null },
                vinculo = vinculo.name,
                fotoCaminho = fotoCaminho,
                criadoEm = anterior?.criadoEm ?: agora,
                alteradoEm = agora,
                apagado = false,
                pendenteEnvio = true,
            )
        )
    }

    /**
     * Exclui uma planta. Ela é só marcada como apagada (não some do banco),
     * para que a sincronização futura consiga avisar os outros aparelhos.
     */
    suspend fun excluir(id: String) {
        val planta = dao.buscarPorId(id) ?: return
        Fotos.apagar(planta.fotoCaminho)
        dao.salvar(
            planta.copy(
                apagado = true,
                fotoCaminho = null,
                alteradoEm = System.currentTimeMillis(),
                pendenteEnvio = true,
            )
        )
    }
}
