// Arquivo: app/src/test/java/br/com/zamypedia/lembretes/CalculoLembretesTest.kt
package br.com.zamypedia.lembretes

import br.com.zamypedia.dados.local.Registro
import br.com.zamypedia.dados.local.TipoRegistro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Testes do cálculo dos lembretes. Rodam no computador, sem celular:
 * no Android Studio, clique com o botão direito neste arquivo e escolha "Run".
 */
class CalculoLembretesTest {
    private fun dia(d: Int) = LocalDate.of(2026, 11, d)

    @Test
    fun `sem padrao com menos de dois registros`() {
        assertNull(CalculoLembretes.prever(emptyList()))
        assertNull(CalculoLembretes.prever(listOf(dia(1))))
        // Duas anotações no mesmo dia contam como uma só
        assertNull(CalculoLembretes.prever(listOf(dia(1), dia(1))))
    }

    @Test
    fun `intervalo medio e proxima data`() {
        // Intervalos de 3 e 5 dias: média 4, próxima = 9 + 4 = 13
        val (intervalo, proxima) = CalculoLembretes.prever(listOf(dia(9), dia(1), dia(4)))!!
        assertEquals(4, intervalo)
        assertEquals(dia(13), proxima)
    }

    @Test
    fun `usa so as ultimas cinco datas`() {
        // As 5 últimas têm intervalo de 2 dias. A primeira (30 dias antes) deve ser ignorada.
        val datas = listOf(LocalDate.of(2026, 10, 1), dia(2), dia(4), dia(6), dia(8), dia(10))
        assertEquals(2, CalculoLembretes.prever(datas)!!.first)
    }

    @Test
    fun `previsoes separam planta e tipo e ignoram floracao`() {
        fun r(planta: String, tipo: TipoRegistro, d: Int) =
            Registro("$planta-$tipo-$d", planta, tipo.name, dia(d).toEpochDay(), 0, 0)

        val lista = listOf(
            r("jiboia", TipoRegistro.REGA, 1), r("jiboia", TipoRegistro.REGA, 4),
            r("jiboia", TipoRegistro.ADUBACAO, 1),
            r("samambaia", TipoRegistro.REGA, 1), r("samambaia", TipoRegistro.REGA, 3),
            r("jiboia", TipoRegistro.FLORACAO, 1), r("jiboia", TipoRegistro.FLORACAO, 20),
        )
        val prev = CalculoLembretes.previsoes(lista)
        assertEquals(2, prev.size)
        assertTrue(prev.any { it.plantaId == "jiboia" && it.tipo == TipoRegistro.REGA && it.proxima == dia(7) })
        assertTrue(prev.any { it.plantaId == "samambaia" && it.intervaloDias == 2 })
    }

    @Test
    fun `textos de prazo`() {
        assertEquals("hoje", CalculoLembretes.descreverPrazo(0))
        assertEquals("amanhã", CalculoLembretes.descreverPrazo(1))
        assertEquals("em 4 dias", CalculoLembretes.descreverPrazo(4))
        assertEquals("atrasado há 2 dias", CalculoLembretes.descreverPrazo(-2))
    }
}
