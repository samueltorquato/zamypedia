// Arquivo: app/src/main/java/br/com/zamypedia/dados/local/Registro.kt
package br.com.zamypedia.dados.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Um registro de cuidado (Bloco 03) ou uma data de floração (Bloco 02).
 * Exemplo: "Reguei a Jiboia em 21/11/2026".
 */
@Entity(tableName = "registro", indices = [Index("planta_id")])
data class Registro(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "planta_id") val plantaId: String,
    // Nome do TipoRegistro (REGA, ADUBACAO, ...)
    @ColumnInfo(name = "tipo") val tipo: String,
    // Dia do cuidado, guardado como número de dias desde 01/01/1970 (formato do LocalDate.toEpochDay)
    @ColumnInfo(name = "data") val dataDia: Long,
    @ColumnInfo(name = "criado_em") val criadoEm: Long,
    @ColumnInfo(name = "alterado_em") val alteradoEm: Long,
    @ColumnInfo(name = "apagado") val apagado: Boolean = false,
    @ColumnInfo(name = "pendente_envio") val pendenteEnvio: Boolean = true,
) {
    val data: LocalDate get() = LocalDate.ofEpochDay(dataDia)
    val tipoEnum: TipoRegistro? get() = TipoRegistro.entries.firstOrNull { it.name == tipo }
}

/**
 * Tipos de registro. Os que têm [cuidado] = true aparecem no Bloco 03 e geram lembretes.
 * [feito] é o texto do botão ("Reguei"), [aFazer] é o texto do lembrete ("Regar").
 */
enum class TipoRegistro(val feito: String, val aFazer: String, val emoji: String, val cuidado: Boolean) {
    REGA("Reguei", "Regar", "💧", true),
    ADUBACAO("Adubei", "Adubar", "🌱", true),
    PODA("Podei", "Podar", "✂️", true),
    TROCA_VASO("Troquei de vaso", "Trocar de vaso", "🪴", true),
    LIMPEZA("Limpei as folhas", "Limpar as folhas", "🍃", true),
    FLORACAO("Floresceu", "", "🌸", false);

    companion object {
        val cuidados: List<TipoRegistro> get() = entries.filter { it.cuidado }
    }
}
