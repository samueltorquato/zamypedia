// Arquivo: app/src/main/java/br/com/zamypedia/ui/comum/SeletorDataParcial.kt
package br.com.zamypedia.ui.comum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

/**
 * Campo de data em que só o ano é obrigatório. Serve para "Desde quando está em sua casa",
 * porque muita gente lembra só o ano, ou o mês e o ano.
 *
 * [aoSalvar] recebe (ano, mês, dia). Mês e dia podem ser null. Tudo null = apagar a informação.
 */
@Composable
fun SeletorDataParcial(
    anoInicial: Int?,
    mesInicial: Int?,
    diaInicial: Int?,
    aoSalvar: (Int?, Int?, Int?) -> Unit,
) {
    // "remember" com as chaves faz os campos se atualizarem quando o valor salvo muda
    var dia by remember(diaInicial) { mutableStateOf(diaInicial?.toString().orEmpty()) }
    var mes by remember(mesInicial) { mutableStateOf(mesInicial?.toString().orEmpty()) }
    var ano by remember(anoInicial) { mutableStateOf(anoInicial?.toString().orEmpty()) }
    var erro by remember { mutableStateOf<String?>(null) }

    // Só deixa digitar números, com no máximo [tamanho] dígitos
    fun soNumeros(texto: String, tamanho: Int) = texto.filter { it.isDigit() }.take(tamanho)

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                dia, { dia = soNumeros(it, 2); erro = null },
                label = { Text("Dia") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(72.dp),
            )
            OutlinedTextField(
                mes, { mes = soNumeros(it, 2); erro = null },
                label = { Text("Mês") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(72.dp),
            )
            OutlinedTextField(
                ano, { ano = soNumeros(it, 4); erro = null },
                label = { Text("Ano") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(96.dp),
            )
        }
        erro?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        OutlinedButton(onClick = {
            val resultado = validar(dia, mes, ano)
            if (resultado.erro != null) erro = resultado.erro
            else aoSalvar(resultado.ano, resultado.mes, resultado.dia)
        }) { Text("Salvar data") }
    }
}

private data class DataParcial(val ano: Int? = null, val mes: Int? = null, val dia: Int? = null, val erro: String? = null)

/** Confere se a combinação faz sentido (ex.: 31/02 não existe) e não está no futuro. */
private fun validar(diaTxt: String, mesTxt: String, anoTxt: String): DataParcial {
    val dia = diaTxt.toIntOrNull()
    val mes = mesTxt.toIntOrNull()
    val ano = anoTxt.toIntOrNull()
    val hoje = LocalDate.now()

    if (ano == null && mes == null && dia == null) return DataParcial() // apagar
    if (ano == null) return DataParcial(erro = "Informe pelo menos o ano.")
    if (ano < 1900 || ano > hoje.year) return DataParcial(erro = "Ano inválido.")
    if (dia != null && mes == null) return DataParcial(erro = "Para informar o dia, informe também o mês.")
    if (mes != null && mes !in 1..12) return DataParcial(erro = "Mês inválido.")
    if (mes != null && YearMonth.of(ano, mes).isAfter(YearMonth.from(hoje))) return DataParcial(erro = "A data não pode estar no futuro.")
    if (dia != null && mes != null) {
        if (dia < 1 || dia > YearMonth.of(ano, mes).lengthOfMonth()) return DataParcial(erro = "Dia inválido.")
        if (LocalDate.of(ano, mes, dia).isAfter(hoje)) return DataParcial(erro = "A data não pode estar no futuro.")
    }
    return DataParcial(ano, mes, dia)
}

/** Texto para mostrar a data parcial: "2021", "03/2021" ou "15/03/2021". */
fun formatarDataParcial(ano: Int?, mes: Int?, dia: Int?): String? = when {
    ano == null -> null
    mes == null -> "$ano"
    dia == null -> "%02d/%d".format(mes, ano)
    else -> "%02d/%02d/%d".format(dia, mes, ano)
}
