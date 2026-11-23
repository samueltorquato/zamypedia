// Arquivo: app/src/main/java/br/com/zamypedia/ui/jardim/TelaPlanta.kt
package br.com.zamypedia.ui.jardim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import br.com.zamypedia.dados.JardimRepositorio
import br.com.zamypedia.dados.local.Floracao
import br.com.zamypedia.dados.local.PlantaJardim
import br.com.zamypedia.dados.local.Registro
import br.com.zamypedia.dados.local.TipoRegistro
import br.com.zamypedia.lembretes.CalculoLembretes
import br.com.zamypedia.lembretes.Previsao
import br.com.zamypedia.ui.comum.SeletorDataParcial
import br.com.zamypedia.ui.comum.formatarDataParcial
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formatoData: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Tela 5: ficha da planta. Cabeçalho, próximos cuidados e três abas:
 * Informações (Bloco 01), História (Bloco 02) e Cuidados (Bloco 03).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPlanta(
    vm: FichaViewModel,
    repositorio: JardimRepositorio,
    idPlanta: String,
    aoVoltar: () -> Unit,
    aoEditar: (String) -> Unit,
) {
    val planta by vm.planta.collectAsState()
    val registros by vm.registros.collectAsState()
    val previsoes by vm.previsoes.collectAsState()
    var aba by rememberSaveable { mutableIntStateOf(0) }
    var confirmarExclusao by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(planta?.nomePopular ?: "") },
            navigationIcon = { TextButton(onClick = aoVoltar) { Text("Voltar") } },
        )
    }) { margens ->
        val p = planta ?: return@Scaffold
        Column(
            Modifier
                .padding(margens)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Cabecalho(p, aoEditar = { aoEditar(p.id) }, aoExcluir = { confirmarExclusao = true })
            ProximosCuidados(previsoes)

            TabRow(selectedTabIndex = aba) {
                listOf("Informações", "História", "Cuidados").forEachIndexed { i, titulo ->
                    Tab(selected = aba == i, onClick = { aba = i }, text = { Text(titulo) })
                }
            }
            Column(Modifier.padding(16.dp)) {
                when (aba) {
                    0 -> AbaInformacoes(p, vm)
                    1 -> AbaHistoria(p, registros.filter { it.tipoEnum == TipoRegistro.FLORACAO }, vm)
                    else -> AbaCuidados(registros.filter { it.tipoEnum?.cuidado == true }, vm)
                }
            }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text("Excluir planta?") },
            text = { Text("A planta, a foto e os registros dela serão removidos do seu jardim.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    escopo.launch {
                        repositorio.excluir(idPlanta)
                        aoVoltar()
                    }
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { confirmarExclusao = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Cabecalho(p: PlantaJardim, aoEditar: () -> Unit, aoExcluir: () -> Unit) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        FotoOuEmoji(p.fotoCaminho, Modifier.size(110.dp), tamanhoEmoji = 48)
        Spacer(Modifier.size(16.dp))
        Column {
            Text(p.nomePopular, style = MaterialTheme.typography.titleLarge)
            p.nomeCientifico?.let { Text(it, fontStyle = FontStyle.Italic) }
            Spacer(Modifier.height(6.dp))
            // Tag colorida do vínculo
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(p.vinculoEnum.corHex))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) { Text(p.vinculoEnum.rotulo, color = Color.White) }
            Row {
                TextButton(onClick = aoEditar) { Text("Editar") }
                TextButton(onClick = aoExcluir) { Text("Excluir") }
            }
        }
    }
}

/** Caixa "Próximos cuidados", calculada a partir dos registros. */
@Composable
private fun ProximosCuidados(previsoes: List<Previsao>) {
    val hoje = LocalDate.now()
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Próximos cuidados", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            if (previsoes.isEmpty()) {
                Text(
                    "Ainda sem previsão. Registre o mesmo cuidado duas vezes na aba Cuidados e o app aprende o ritmo da planta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                previsoes.forEach { prev ->
                    val dias = prev.diasAte(hoje)
                    Text(
                        "${prev.tipo.emoji} ${prev.tipo.aFazer}: ${CalculoLembretes.descreverPrazo(dias)} " +
                            "(a cada ${prev.intervaloDias} ${if (prev.intervaloDias == 1) "dia" else "dias"})",
                        color = if (dias < 0) MaterialTheme.colorScheme.error else Color.Unspecified,
                    )
                }
            }
        }
    }
}

/** Bloco 01: informações escritas pela própria pessoa (nesta fase, sem ficha oficial). */
@Composable
private fun AbaInformacoes(p: PlantaJardim, vm: FichaViewModel) {
    val salvo = InformacoesPlanta.de(p)
    // Começa com o que está salvo. Só recomeça se abrir outra planta.
    var info by remember(p.id) { mutableStateOf(salvo) }
    val mudou = info != salvo

    Campo("Taxonomia (família, gênero)", info.taxonomia) { info = info.copy(taxonomia = it) }
    Campo("Luz", info.luz) { info = info.copy(luz = it) }
    Campo("Água", info.agua) { info = info.copy(agua = it) }
    Campo("Solo", info.solo) { info = info.copy(solo = it) }
    Campo("Nutrientes", info.nutrientes) { info = info.copy(nutrientes = it) }
    Campo("Sinais nas folhas", info.sinaisFolhas) { info = info.copy(sinaisFolhas = it) }
    Campo("Pragas e doenças", info.sinaisPragas) { info = info.copy(sinaisPragas = it) }

    Button(onClick = { vm.salvarInformacoes(info) }, enabled = mudou) {
        Text(if (mudou) "Salvar informações" else "Informações salvas ✓")
    }
}

/** Um campo de texto do Bloco 01. */
@Composable
private fun Campo(rotulo: String, valor: String, aoMudar: (String) -> Unit) {
    OutlinedTextField(
        valor, aoMudar,
        label = { Text(rotulo) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        maxLines = 5,
    )
}

/** Bloco 02: desde quando está em casa, situação de floração e datas em que floresceu. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AbaHistoria(p: PlantaJardim, floracoes: List<Registro>, vm: FichaViewModel) {
    var escolherData by remember { mutableStateOf(false) }

    Text("Desde quando está em sua casa", style = MaterialTheme.typography.titleSmall)
    Text(
        formatarDataParcial(p.desdeAno, p.desdeMes, p.desdeDia) ?: "Não informado. Só o ano é obrigatório.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    SeletorDataParcial(p.desdeAno, p.desdeMes, p.desdeDia) { ano, mes, dia -> vm.salvarDesde(ano, mes, dia) }

    Spacer(Modifier.height(20.dp))
    HorizontalDivider()
    Spacer(Modifier.height(12.dp))

    Text("Floração", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Floracao.entries.forEach { f ->
            val marcado = p.floracaoEnum == f
            // Tocar de novo na opção marcada desmarca
            FilterChip(selected = marcado, onClick = { vm.salvarFloracao(if (marcado) null else f) }, label = { Text(f.rotulo) })
        }
    }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = { escolherData = true }) { Text("🌸 Anotar floração") }
    Spacer(Modifier.height(8.dp))
    if (floracoes.isEmpty()) {
        Text("Nenhuma floração anotada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        floracoes.forEach { LinhaRegistro(it, vm) }
    }

    if (escolherData) {
        DialogoData(
            titulo = "Quando floresceu?",
            aoConfirmar = { data -> vm.adicionarRegistro(TipoRegistro.FLORACAO, data) },
            aoFechar = { escolherData = false },
        )
    }
}

/** Bloco 03: botões de cuidado e o histórico deles. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AbaCuidados(cuidados: List<Registro>, vm: FichaViewModel) {
    var tipoEscolhido by remember { mutableStateOf<TipoRegistro?>(null) }

    Text("Registrar um cuidado", style = MaterialTheme.typography.titleSmall)
    Text(
        "A data começa em hoje. Dá para escolher outro dia, se esqueceu de anotar.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TipoRegistro.cuidados.forEach { tipo ->
            OutlinedButton(onClick = { tipoEscolhido = tipo }) { Text("${tipo.emoji} ${tipo.feito}") }
        }
    }

    Spacer(Modifier.height(16.dp))
    Text("Histórico", style = MaterialTheme.typography.titleSmall)
    if (cuidados.isEmpty()) {
        Text("Nenhum cuidado registrado ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        cuidados.forEach { LinhaRegistro(it, vm) }
    }

    tipoEscolhido?.let { tipo ->
        DialogoData(
            titulo = "${tipo.emoji} ${tipo.feito} em",
            aoConfirmar = { data -> vm.adicionarRegistro(tipo, data) },
            aoFechar = { tipoEscolhido = null },
        )
    }
}

/** Uma linha do histórico, com opção de apagar um registro feito por engano. */
@Composable
private fun LinhaRegistro(r: Registro, vm: FichaViewModel) {
    var confirmar by remember { mutableStateOf(false) }
    val tipo = r.tipoEnum ?: return
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("${tipo.emoji} ${tipo.feito} em ${r.data.format(formatoData)}", Modifier.weight(1f))
        TextButton(onClick = { confirmar = true }) { Text("Apagar") }
    }
    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Apagar registro?") },
            text = { Text("${tipo.feito} em ${r.data.format(formatoData)}") },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    vm.apagarRegistro(r.id)
                }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
        )
    }
}

/**
 * Calendário para escolher o dia, começando em hoje. Dias futuros ficam bloqueados.
 * O calendário do Material trabalha com milissegundos em UTC, por isso as conversões abaixo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoData(titulo: String, aoConfirmar: (LocalDate) -> Unit, aoFechar: () -> Unit) {
    val hoje = LocalDate.now()
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = paraMillisUtc(hoje),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !deMillisUtc(utcTimeMillis).isAfter(hoje)
            override fun isSelectableYear(year: Int) = year <= hoje.year
        },
    )
    DatePickerDialog(
        onDismissRequest = aoFechar,
        confirmButton = {
            TextButton(onClick = {
                estado.selectedDateMillis?.let { aoConfirmar(deMillisUtc(it)) }
                aoFechar()
            }) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar") } },
    ) {
        DatePicker(
            state = estado,
            title = { Text(titulo, Modifier.padding(start = 24.dp, top = 16.dp)) },
        )
    }
}

private fun paraMillisUtc(data: LocalDate): Long = data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun deMillisUtc(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
