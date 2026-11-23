// Arquivo: app/src/main/java/br/com/zamypedia/ui/jardim/TelaAdicionarPlanta.kt
package br.com.zamypedia.ui.jardim

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import br.com.zamypedia.dados.local.Vinculo
import br.com.zamypedia.util.Fotos

/** Tela 4: Adicionar (ou editar) planta: nomes, vínculo e uma foto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAdicionarPlanta(vm: EdicaoPlantaViewModel, aoTerminar: () -> Unit) {
    val context = LocalContext.current

    // Galeria: usa o seletor de fotos do Android, que não exige permissão de acesso aos arquivos
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.aoEscolherFoto(context, uri)
    }

    // Câmera: o app de câmera do celular grava a foto num arquivo temporário nosso
    var uriCamera by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tirou ->
        val uri = uriCamera
        if (tirou && uri != null) vm.aoEscolherFoto(context, uri)
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (vm.editando) "Editar planta" else "Adicionar planta") },
            navigationIcon = { TextButton(onClick = aoTerminar) { Text("Cancelar") } },
        )
    }) { margens ->
        Column(
            Modifier
                .padding(margens)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            // Foto
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FotoOuEmoji(vm.fotoCaminho, Modifier.size(160.dp), tamanhoEmoji = 64)
                if (vm.processandoFoto) CircularProgressIndicator()
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                OutlinedButton(onClick = {
                    val arquivo = Fotos.arquivoTemporarioDaCamera(context)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.arquivos", arquivo)
                    uriCamera = uri
                    camera.launch(uri)
                }) { Text("📷 Câmera") }
                OutlinedButton(onClick = {
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("🖼️ Galeria") }
                if (vm.fotoCaminho != null) TextButton(onClick = vm::removerFoto) { Text("Remover") }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = vm.nomePopular,
                onValueChange = { vm.nomePopular = it },
                label = { Text("Nome popular *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.nomeCientifico,
                onValueChange = { vm.nomeCientifico = it },
                label = { Text("Nome científico (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Text("Vínculo com a planta", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Vinculo.entries.forEach { v ->
                    FilterChip(
                        selected = vm.vinculo == v,
                        onClick = { vm.vinculo = v },
                        label = { Text(v.rotulo, color = Color(v.corHex)) },
                    )
                }
            }

            vm.mensagem?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.salvar(aoTerminar) },
                enabled = !vm.processandoFoto,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar") }
        }
    }
}
