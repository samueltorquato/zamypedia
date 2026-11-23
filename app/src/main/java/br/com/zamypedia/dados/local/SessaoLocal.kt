// Arquivo: app/src/main/java/br/com/zamypedia/dados/local/SessaoLocal.kt
package br.com.zamypedia.dados.local

import android.content.Context

/**
 * Guarda no aparelho quem está logado (id da conta e @).
 *
 * Isso permite abrir o app direto no Meu Jardim mesmo sem internet,
 * sem depender de renovar o login na nuvem a cada abertura.
 */
class SessaoLocal(context: Context) {
    private val prefs = context.getSharedPreferences("sessao", Context.MODE_PRIVATE)

    val usuarioId: String? get() = prefs.getString("usuario_id", null)
    val arroba: String? get() = prefs.getString("arroba", null)
    val estaLogado: Boolean get() = usuarioId != null

    fun salvar(usuarioId: String, arroba: String) {
        prefs.edit().putString("usuario_id", usuarioId).putString("arroba", arroba).apply()
    }

    fun limpar() {
        prefs.edit().clear().apply()
    }
}
