// Arquivo: app/src/main/java/br/com/zamypedia/lembretes/CalculoLembretes.kt
package br.com.zamypedia.lembretes

import br.com.zamypedia.dados.local.Registro
import br.com.zamypedia.dados.local.TipoRegistro
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

/** A previsão de um cuidado: por exemplo, "regar a Jiboia a cada 3 dias, próxima em 14/12". */
data class Previsao(
    val plantaId: String,
    val tipo: TipoRegistro,
    val intervaloDias: Int,
    val ultima: LocalDate,
    val proxima: LocalDate,
) {
    /** Quantos dias faltam (negativo = atrasado). */
    fun diasAte(hoje: LocalDate): Long = ChronoUnit.DAYS.between(hoje, proxima)
}

/**
 * Aprende o ritmo de cada cuidado a partir das datas registradas (decisão do plano da segunda entrega):
 * 1. pega as últimas 5 datas daquele cuidado naquela planta;
 * 2. calcula o intervalo médio entre elas;
 * 3. próxima data = última data + intervalo médio.
 * Com menos de 2 datas ainda não há padrão, então não há lembrete (decidido por Samuel em 23/11/2026).
 */
object CalculoLembretes {
    private const val QUANTIDADE_DE_DATAS = 5

    /** Devolve o intervalo médio em dias e a próxima data, ou null se ainda não houver padrão. */
    fun prever(datas: List<LocalDate>): Pair<Int, LocalDate>? {
        // Duas anotações no mesmo dia contam como uma só
        val ultimas = datas.distinct().sorted().takeLast(QUANTIDADE_DE_DATAS)
        if (ultimas.size < 2) return null
        val intervalos = ultimas.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b) }
        val media = max(1, intervalos.average().roundToInt())
        return media to ultimas.last().plusDays(media.toLong())
    }

    /** Previsões de todos os cuidados de todas as plantas presentes em [registros]. */
    fun previsoes(registros: List<Registro>): List<Previsao> =
        registros
            .filter { it.tipoEnum?.cuidado == true }
            .groupBy { it.plantaId to it.tipoEnum!! }
            .mapNotNull { (chave, lista) ->
                val (plantaId, tipo) = chave
                prever(lista.map { it.data })?.let { (intervalo, proxima) ->
                    Previsao(plantaId, tipo, intervalo, lista.maxOf { it.data }, proxima)
                }
            }

    /** Texto amigável para a ficha: "hoje", "amanhã", "em 3 dias", "atrasado há 2 dias". */
    fun descreverPrazo(dias: Long): String = when {
        dias == 0L -> "hoje"
        dias == 1L -> "amanhã"
        dias > 1 -> "em $dias dias"
        dias == -1L -> "atrasado há 1 dia"
        else -> "atrasado há ${-dias} dias"
    }
}
