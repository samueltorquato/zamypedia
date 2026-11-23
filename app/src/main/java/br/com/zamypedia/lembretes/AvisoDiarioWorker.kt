// Arquivo: app/src/main/java/br/com/zamypedia/lembretes/AvisoDiarioWorker.kt
package br.com.zamypedia.lembretes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.zamypedia.MainActivity
import br.com.zamypedia.ZamypediaApp
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Tarefa que o Android roda uma vez por dia, no horário escolhido.
 * Ela confere quais cuidados vencem hoje (ou estão atrasados) e mostra UM aviso com todos eles.
 * Funciona sem internet: lê só o banco do celular.
 */
class AvisoDiarioWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ZamypediaApp
        val donoId = app.sessao.usuarioId ?: return Result.success()
        val prefs = app.preferenciasLembretes

        val hoje = LocalDate.now()
        val nomes = app.jardim.listarDoDono(donoId).associate { it.id to it.nomePopular }
        val vencidos = CalculoLembretes.previsoes(app.registros.todosDoDono(donoId))
            .filter { it.plantaId in nomes && prefs.ligado(it.tipo) && it.diasAte(hoje) <= 0 }

        if (vencidos.isNotEmpty()) mostrarAviso(vencidos, nomes)
        return Result.success()
    }

    /** Monta o texto, por exemplo "Regar: Jiboia, Samambaia. Adubar: Espada de São Jorge." */
    private fun mostrarAviso(vencidos: List<Previsao>, nomes: Map<String, String>) {
        val contexto = applicationContext
        // Sem permissão de notificação (Android 13 ou mais novo), não há o que fazer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val texto = vencidos.groupBy { it.tipo }.entries.joinToString(". ") { (tipo, lista) ->
            "${tipo.aFazer}: " + lista.mapNotNull { nomes[it.plantaId] }.distinct().joinToString(", ")
        } + "."

        criarCanal(contexto)
        val abrirApp = PendingIntent.getActivity(
            contexto, 0,
            Intent(contexto, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val aviso = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Cuidados de hoje 🪴")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(contexto).notify(ID_AVISO, aviso)
    }

    companion object {
        private const val CANAL = "lembretes"
        private const val ID_AVISO = 1
        private const val NOME_TAREFA = "aviso_diario"

        /** O "canal" é a categoria que aparece nas configurações de notificação do Android. */
        fun criarCanal(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val canal = NotificationChannel(CANAL, "Lembretes de cuidados", NotificationManager.IMPORTANCE_DEFAULT)
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
            }
        }

        /**
         * Agenda a tarefa diária para o horário escolhido.
         * [reagendar] = true quando o horário mudou, para valer a nova hora.
         */
        fun agendar(context: Context, hora: Int, reagendar: Boolean = false) {
            val agora = LocalDateTime.now()
            var proxima = agora.toLocalDate().atTime(hora, 0)
            if (!proxima.isAfter(agora)) proxima = proxima.plusDays(1)
            val espera = Duration.between(agora, proxima).toMinutes()

            val pedido = PeriodicWorkRequestBuilder<AvisoDiarioWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(espera, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOME_TAREFA,
                if (reagendar) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP,
                pedido,
            )
        }
    }
}
