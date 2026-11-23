// Arquivo: app/src/main/java/br/com/zamypedia/dados/local/RegistroDao.kt
package br.com.zamypedia.dados.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Consultas da tabela de registros. */
@Dao
interface RegistroDao {

    /** Registros de uma planta, do mais recente para o mais antigo. */
    @Query("SELECT * FROM registro WHERE planta_id = :plantaId AND apagado = 0 ORDER BY data DESC, criado_em DESC")
    fun observarDaPlanta(plantaId: String): Flow<List<Registro>>

    /** Todos os registros das plantas (não excluídas) de uma pessoa. Usado pelo aviso diário. */
    @Query(
        """
        SELECT r.* FROM registro r
        INNER JOIN planta_jardim p ON p.id = r.planta_id
        WHERE p.dono_id = :donoId AND p.apagado = 0 AND r.apagado = 0
        """
    )
    suspend fun listarDoDono(donoId: String): List<Registro>

    @Query("SELECT * FROM registro WHERE id = :id")
    suspend fun buscarPorId(id: String): Registro?

    @Upsert
    suspend fun salvar(registro: Registro)
}
