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

    // ----- Bloco 01: informações da planta (versão 2 do banco). Todos opcionais. -----
    @ColumnInfo(name = "taxonomia") val taxonomia: String? = null,
    @ColumnInfo(name = "luz") val luz: String? = null,
    @ColumnInfo(name = "agua") val agua: String? = null,
    @ColumnInfo(name = "solo") val solo: String? = null,
    @ColumnInfo(name = "nutrientes") val nutrientes: String? = null,
    @ColumnInfo(name = "sinais_folhas") val sinaisFolhas: String? = null,
    @ColumnInfo(name = "sinais_pragas") val sinaisPragas: String? = null,

    // ----- Bloco 02: desde quando está em casa (mês e dia podem ficar vazios) e floração -----
    @ColumnInfo(name = "desde_ano") val desdeAno: Int? = null,
    @ColumnInfo(name = "desde_mes") val desdeMes: Int? = null,
    @ColumnInfo(name = "desde_dia") val desdeDia: Int? = null,
    // ESTA_FLORIDA, JA_FLORESCEU ou NUNCA (ver enum Floracao)
    @ColumnInfo(name = "floracao") val floracao: String? = null,
) {
    val vinculoEnum: Vinculo get() = Vinculo.deTexto(vinculo)
    val floracaoEnum: Floracao? get() = Floracao.entries.firstOrNull { it.name == floracao }
}

/** Situação de floração do Bloco 02. */
enum class Floracao(val rotulo: String) {
    ESTA_FLORIDA("Está florida"),
    JA_FLORESCEU("Já floresceu"),
    NUNCA("Nunca floresceu"),
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
