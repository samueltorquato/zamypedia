// Arquivo: app/src/main/java/br/com/zamypedia/dados/nuvem/Supabase.kt
package br.com.zamypedia.dados.nuvem

import br.com.zamypedia.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Cria a conexão com o Supabase.
 *
 * O endereço e a chave vêm do local.properties (ver local.properties.exemplo).
 * A chave usada é a "anon public": pode ficar no app porque quem protege os dados
 * são as regras de segurança (RLS) criadas pelo script supabase/001_perfil.sql.
 */
object Supabase {
    fun criarCliente(): SupabaseClient {
        val url = BuildConfig.SUPABASE_URL.trim().removeSuffix("/").removeSuffix("/rest/v1")
        val chave = BuildConfig.SUPABASE_ANON_KEY.trim()
        // Avisa claramente quando o local.properties não foi preenchido (ou o app não foi recompilado depois)
        if (!url.startsWith("https://") || chave.isEmpty()) {
            throw IllegalStateException(
                "Supabase não configurado. Confira supabase.url e supabase.chave_publica no local.properties e rode o app de novo."
            )
        }
        return createSupabaseClient(supabaseUrl = url, supabaseKey = chave) {
            install(Auth)
            install(Postgrest)
        }
    }
}
