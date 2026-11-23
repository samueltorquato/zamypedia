// Arquivo: app/src/main/java/br/com/zamypedia/dados/RegistrosRepositorio.kt
package br.com.zamypedia.dados

import br.com.zamypedia.dados.local.Registro
import br.com.zamypedia.dados.local.RegistroDao
import br.com.zamypedia.dados.local.TipoRegistro
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

/** Gravar, apagar e listar os registros de cuidado e de floração. Tudo fica no celular. */
class RegistrosRepositorio(private val dao: RegistroDao) {

    fun daPlanta(plantaId: String): Flow<List<Registro>> = dao.observarDaPlanta(plantaId)

    suspend fun todosDoDono(donoId: String): List<Registro> = dao.listarDoDono(donoId)

    suspend fun registrar(plantaId: String, tipo: TipoRegistro, data: LocalDate) {
        val agora = System.currentTimeMillis()
        dao.salvar(
            Registro(
                id = UUID.randomUUID().toString(),
                plantaId = plantaId,
                tipo = tipo.name,
                dataDia = data.toEpochDay(),
                criadoEm = agora,
                alteradoEm = agora,
            )
        )
    }

    /** Apaga um registro feito por engano (só marca como apagado, para a sincronização futura). */
    suspend fun apagar(id: String) {
        val registro = dao.buscarPorId(id) ?: return
        dao.salvar(registro.copy(apagado = true, alteradoEm = System.currentTimeMillis(), pendenteEnvio = true))
    }
}
