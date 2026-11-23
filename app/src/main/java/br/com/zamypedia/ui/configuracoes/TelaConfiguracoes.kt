// Arquivo: app/src/main/java/br/com/zamypedia/ui/configuracoes/TelaConfiguracoes.kt
package br.com.zamypedia.ui.configuracoes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import br.com.zamypedia.dados.local.TipoRegistro
import br.com.zamypedia.lembretes.AvisoDiarioWorker
import br.com.zamypedia.lembretes.PreferenciasLembretes

/** Configurações: quais lembretes avisar, horário do aviso diário e sair da conta. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConfiguracoes(prefs: PreferenciasLembretes, aoVoltar: () -> Unit, aoSair: () -> Unit) {
    val contexto = LocalContext.current
    var hora by remember { mutableStateOf(prefs.hora) }
    var menuHora by remember { mutableStateOf(false) }
    // Um estado por tipo, para o interruptor mudar na hora
    val ligados = remember { TipoRegistro.cuidados.associateWith { mutableStateOf(prefs.ligado(it)) } }

    // Confere de novo a permissão sempre que a pessoa volta das configurações do Android
    var notificacoesPermitidas by remember { mutableStateOf(notificacoesPermitidas(contexto)) }
    LifecycleResumeEffect(Unit) {
        notificacoesPermitidas = notificacoesPermitidas(contexto)
        onPauseOrDispose { }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Configurações") },
            navigationIcon = { TextButton(onClick = aoVoltar) { Text("Voltar") } },
        )
    }) { margens ->
        Column(
            Modifier
                .padding(margens)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Lembretes", style = MaterialTheme.typography.titleMedium)
            Text(
                "O app aprende o ritmo de cada cuidado depois de dois registros e avisa uma vez por dia, " +
                    "juntando tudo o que vence naquele dia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!notificacoesPermitidas) {
                Spacer(Modifier.height(8.dp))
                Text("⚠️ As notificações do Zamypedia estão desligadas no celular.", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { abrirConfiguracoesDeNotificacao(contexto) }) { Text("Ligar notificações") }
            }

            Spacer(Modifier.height(12.dp))
            TipoRegistro.cuidados.forEach { tipo ->
                val estado = ligados.getValue(tipo)
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${tipo.emoji} ${tipo.aFazer}", Modifier.weight(1f))
                    Switch(checked = estado.value, onCheckedChange = {
                        estado.value = it
                        prefs.definirLigado(tipo, it)
                    })
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Horário do aviso diário", Modifier.weight(1f))
                Box {
                    OutlinedButton(onClick = { menuHora = true }) { Text("%02d:00 ▾".format(hora)) }
                    DropdownMenu(expanded = menuHora, onDismissRequest = { menuHora = false }) {
                        (0..23).forEach { h ->
                            DropdownMenuItem(text = { Text("%02d:00".format(h)) }, onClick = {
                                menuHora = false
                                hora = h
                                prefs.hora = h
                                // Reagenda para o novo horário passar a valer
                                AvisoDiarioWorker.agendar(contexto, h, reagendar = true)
                            })
                        }
                    }
                }
            }
            Text(
                "O Android pode atrasar o aviso alguns minutos para economizar bateria.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Conta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = aoSair) { Text("Sair da conta") }
        }
    }
}

/** No Android 12 ou mais antigo a permissão é automática, mas a pessoa pode desligar nas configurações. */
private fun notificacoesPermitidas(contexto: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return false
    return NotificationManagerCompat.from(contexto).areNotificationsEnabled()
}

/** Abre a página de notificações do Zamypedia nas configurações do Android. */
private fun abrirConfiguracoesDeNotificacao(contexto: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    contexto.startActivity(intent)
}
