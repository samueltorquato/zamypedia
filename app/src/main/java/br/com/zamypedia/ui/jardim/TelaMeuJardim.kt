// Arquivo: app/src/main/java/br/com/zamypedia/ui/jardim/TelaMeuJardim.kt
package br.com.zamypedia.ui.jardim

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.zamypedia.dados.local.PlantaJardim
import br.com.zamypedia.dados.local.Vinculo
import coil.compose.AsyncImage
import java.io.File

/** Tela 3: Meu Jardim. Grade de três colunas, filtro por vínculo e ordenação. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMeuJardim(vm: JardimViewModel, aoAdicionar: () -> Unit, aoAbrirPlanta: (String) -> Unit) {
    val plantas by vm.plantas.collectAsState()
    val filtro by vm.filtro.collectAsState()
    val ordenacao by vm.ordenacao.collectAsState()
    var menuAberto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Jardim") },
                actions = {
                    // Menu de ordenação
                    Box {
                        TextButton(onClick = { menuAberto = true }) { Text("Ordenar ▾") }
                        DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                            Ordenacao.entries.forEach { opcao ->
                                DropdownMenuItem(
                                    text = { Text((if (opcao == ordenacao) "✓ " else "") + opcao.rotulo) },
                                    onClick = {
                                        vm.ordenacao.value = opcao
                                        menuAberto = false
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            // Botão (+) um pouco acima do canto inferior direito, como pede o documento
            FloatingActionButton(onClick = aoAdicionar, containerColor = MaterialTheme.colorScheme.primary) {
                Text("+", fontSize = 28.sp, color = Color.White)
            }
        },
    ) { margens ->
        Column(Modifier.padding(margens).fillMaxSize()) {
            // Barra secundária de vínculo: Todas, Tenho, Já tive, Quero ter
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = filtro == null, onClick = { vm.filtro.value = null }, label = { Text("Todas") })
                Vinculo.entries.forEach { v ->
                    FilterChip(
                        selected = filtro == v,
                        onClick = { vm.filtro.value = v },
                        label = { Text("🌿 ${v.rotulo}", color = Color(v.corHex)) },
                    )
                }
            }

            if (plantas.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (filtro == null) "Seu jardim ainda está vazio.\nToque em + para adicionar a primeira planta."
                        else "Nenhuma planta com este vínculo.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                // Três colunas. Cabem cerca de nove itens na tela, o resto aparece ao rolar.
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(plantas, key = { it.id }) { planta ->
                        CartaoPlanta(planta) { aoAbrirPlanta(planta.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoPlanta(planta: PlantaJardim, aoTocar: () -> Unit) {
    Column(Modifier.clickable(onClick = aoTocar)) {
        FotoOuEmoji(planta.fotoCaminho, Modifier.fillMaxWidth().aspectRatio(0.75f))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Tag colorida do vínculo
            Box(Modifier.size(8.dp).clip(CircleShape).background(Color(planta.vinculoEnum.corHex)))
            Spacer(Modifier.size(4.dp))
            Text(
                planta.nomePopular,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Mostra a foto da planta, ou um vaso de planta quando não há foto. */
@Composable
fun FotoOuEmoji(caminho: String?, modifier: Modifier = Modifier, tamanhoEmoji: Int = 36) {
    val forma = RoundedCornerShape(10.dp)
    if (caminho != null) {
        AsyncImage(
            model = File(caminho),
            contentDescription = "Foto da planta",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(forma),
        )
    } else {
        Box(
            modifier.clip(forma).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text("🪴", fontSize = tamanhoEmoji.sp) }
    }
}
