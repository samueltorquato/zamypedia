// Arquivo: app/src/main/java/br/com/zamypedia/dados/local/PlantaJardim.kt
package br.com.zamypedia.dados.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Uma planta do Meu Jardim, como fica guardada no banco do aparelho.
 *
 * As colunas alterado_em, apagado e pendente_envio ainda não são usadas pela nuvem,
 * mas já existem para que a sincronização futura não exija mudar o banco.
 */
@Entity(tableName = "planta_jardim")
data class PlantaJardim(
    // Identificador criado no próprio aparelho (UUID), assim dois celulares nunca geram o mesmo id
    @PrimaryKey val id: String,
    @ColumnInfo(name = "dono_id") val donoId: String,
    @ColumnInfo(name = "nome_popular") val nomePopular: String,
    @ColumnInfo(name = "nome_cientifico") val nomeCientifico: String?,
    // Guardamos o nome do Vinculo (TENHO, JA_TIVE ou QUERO_TER) como texto
    @ColumnInfo(name = "vinculo") val vinculo: String,
    // Caminho do arquivo .webp já comprimido, dentro da pasta privada do app
    @ColumnInfo(name = "foto_caminho") val fotoCaminho: String?,
    // Datas em milissegundos desde 1970 (formato padrão do Android)
    @ColumnInfo(name = "criado_em") val criadoEm: Long,
    @ColumnInfo(name = "alterado_em") val alteradoEm: Long,
    @ColumnInfo(name = "apagado") val apagado: Boolean = false,
    @ColumnInfo(name = "pendente_envio") val pendenteEnvio: Boolean = true,
) {
    val vinculoEnum: Vinculo get() = Vinculo.deTexto(vinculo)
}

/** As três opções de vínculo do PDF de requisitos, cada uma com sua cor de tag. */
enum class Vinculo(val rotulo: String, val corHex: Long) {
    TENHO("Tenho", 0xFF6F7F5F),      // Verde Oliva
    JA_TIVE("Já tive", 0xFFB08D57),  // Dourado, combina com o Champagne
    QUERO_TER("Quero ter", 0xFF7C9BB0); // Azul acinzentado, para contrastar com os verdes

    companion object {
        fun deTexto(texto: String): Vinculo = entries.firstOrNull { it.name == texto } ?: TENHO
    }
}
