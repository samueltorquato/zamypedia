// Arquivo: app/src/main/java/br/com/zamypedia/dados/local/PlantaJardimDao.kt
package br.com.zamypedia.dados.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Consultas ao banco local. "Flow" faz a tela se atualizar sozinha quando os dados mudam. */
@Dao
interface PlantaJardimDao {

    @Query("SELECT * FROM planta_jardim WHERE dono_id = :donoId AND apagado = 0")
    fun observarDoDono(donoId: String): Flow<List<PlantaJardim>>

    @Query("SELECT * FROM planta_jardim WHERE id = :id AND apagado = 0")
    fun observarPorId(id: String): Flow<PlantaJardim?>

    @Query("SELECT * FROM planta_jardim WHERE id = :id")
    suspend fun buscarPorId(id: String): PlantaJardim?

    /** Insere uma planta nova ou substitui a existente com o mesmo id. */
    @Upsert
    suspend fun salvar(planta: PlantaJardim)
}
