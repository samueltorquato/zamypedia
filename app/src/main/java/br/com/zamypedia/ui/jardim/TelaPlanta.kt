// Arquivo: app/src/main/java/br/com/zamypedia/ui/jardim/TelaPlanta.kt
package br.com.zamypedia.ui.jardim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import br.com.zamypedia.dados.JardimRepositorio
import kotlinx.coroutines.launch

/**
 * Tela 5: visão simples de uma planta (foto, nomes, tag do vínculo, editar e excluir).
 * A ficha completa com os Blocos 01, 02 e 03 chega na próxima entrega.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPlanta(
    repositorio: JardimRepositorio,
    idPlanta: String,
    aoVoltar: () -> Unit,
    aoEditar: (String) -> Unit,
) {
    val planta by remember(idPlanta) { repositorio.planta(idPlanta) }.collectAsState(initial = null)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            FotoOuEmoji(p.fotoCaminho, Modifier.fillMaxWidth().aspectRatio(1f), tamanhoEmoji = 96)
            Spacer(Modifier.height(16.dp))
            Text(p.nomePopular, style = MaterialTheme.typography.headlineSmall)
            p.nomeCientifico?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
            }
            Spacer(Modifier.height(8.dp))
            // Tag colorida do vínculo
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(p.vinculoEnum.corHex))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) { Text(p.vinculoEnum.rotulo, color = Color.White) }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { aoEditar(p.id) }) { Text("Editar") }
                OutlinedButton(onClick = { confirmarExclusao = true }) { Text("Excluir") }
            }
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text("Excluir planta?") },
            text = { Text("A planta e a foto dela serão removidas do seu jardim.") },
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
