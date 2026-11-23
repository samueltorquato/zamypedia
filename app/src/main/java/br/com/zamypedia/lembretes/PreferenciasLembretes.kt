// Arquivo: app/src/main/java/br/com/zamypedia/lembretes/PreferenciasLembretes.kt
package br.com.zamypedia.lembretes

import android.content.Context
import br.com.zamypedia.dados.local.TipoRegistro

/** Quais tipos de lembrete estão ligados e o horário do aviso diário. Guardado só no celular. */
class PreferenciasLembretes(context: Context) {
    private val prefs = context.getSharedPreferences("lembretes", Context.MODE_PRIVATE)

    /** Todos os tipos começam ligados. */
    fun ligado(tipo: TipoRegistro): Boolean = prefs.getBoolean("ligado_${tipo.name}", true)

    fun definirLigado(tipo: TipoRegistro, ligado: Boolean) {
        prefs.edit().putBoolean("ligado_${tipo.name}", ligado).apply()
    }

    /** Hora do aviso diário, de 0 a 23. Padrão: 8h (decidido em 23/11/2026). */
    var hora: Int
        get() = prefs.getInt("hora", 8)
        set(valor) = prefs.edit().putInt("hora", valor).apply()

    /** Marca se o app já pediu a permissão de notificações, para não pedir toda vez. */
    var jaPediuPermissao: Boolean
        get() = prefs.getBoolean("ja_pediu_permissao", false)
        set(valor) = prefs.edit().putBoolean("ja_pediu_permissao", valor).apply()
}
