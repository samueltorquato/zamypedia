// Arquivo: app/src/main/java/br/com/zamypedia/dados/nuvem/ContaRepositorio.kt
package br.com.zamypedia.dados.nuvem

import br.com.zamypedia.dados.local.SessaoLocal
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Resultado do cadastro: entrou direto, ou precisa confirmar o e-mail antes. */
enum class ResultadoCadastro { CONTA_CRIADA_E_LOGADA, CONFIRMAR_EMAIL }

/**
 * Tudo que envolve a conta: criar, entrar, sair e conferir se um @ está livre.
 * Estas são as únicas operações da primeira entrega que precisam de internet.
 */
class ContaRepositorio(
    private val supabase: SupabaseClient,
    private val sessao: SessaoLocal,
) {
    /** Regra do @: 3 a 20 caracteres, só letras minúsculas, números, ponto e sublinhado. */
    fun arrobaValido(arroba: String): Boolean = Regex("^[a-z0-9_.]{3,20}$").matches(arroba)

    /** Pergunta ao banco se o @ está livre. A resposta é só sim ou não. */
    suspend fun arrobaDisponivel(arroba: String): Boolean =
        supabase.postgrest
            .rpc("arroba_disponivel", buildJsonObject { put("p_arroba", arroba) })
            .decodeAs<Boolean>()

    suspend fun criarConta(email: String, senha: String, arroba: String): ResultadoCadastro {
        // O @ vai junto como "metadado" da conta. Um gatilho no banco usa esse valor
        // para criar a linha na tabela perfil (ver supabase/001_perfil.sql).
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = senha
            this.data = buildJsonObject { put("arroba", arroba) }
        }
        val usuario = supabase.auth.currentUserOrNull()
        return if (usuario != null) {
            sessao.salvar(usuario.id, arroba)
            ResultadoCadastro.CONTA_CRIADA_E_LOGADA
        } else {
            // Com a confirmação de e-mail ligada no Supabase, a pessoa só entra depois de confirmar
            ResultadoCadastro.CONFIRMAR_EMAIL
        }
    }

    suspend fun entrar(email: String, senha: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = senha
        }
        val usuario = supabase.auth.currentUserOrNull()
            ?: error("Não foi possível entrar. Tente de novo.")
        val arroba = usuario.userMetadata?.get("arroba")?.jsonPrimitive?.content ?: ""
        sessao.salvar(usuario.id, arroba)
    }

    suspend fun sair() {
        // Limpa a sessão local primeiro, assim a saída funciona mesmo sem internet
        sessao.limpar()
        runCatching { supabase.auth.signOut() }
    }
}

/** Traduz os erros mais comuns do Supabase para mensagens em português. */
fun mensagemDeErro(erro: Throwable): String {
    val texto = (erro.message ?: "").lowercase()
    return when {
        "invalid login credentials" in texto -> "E-mail ou senha incorretos."
        "email not confirmed" in texto -> "Confirme seu e-mail antes de entrar. Veja sua caixa de entrada."
        "already registered" in texto || "already been registered" in texto -> "Este e-mail já tem uma conta."
        "database error saving new user" in texto -> "Este @ acabou de ser usado por outra pessoa. Escolha outro."
        "password" in texto && ("weak" in texto || "at least" in texto) -> "Senha fraca. Use pelo menos 8 caracteres."
        "unable to resolve host" in texto || "failed to connect" in texto || "timeout" in texto ||
            erro is java.io.IOException -> "Sem conexão com a internet. Criar conta e entrar precisam de internet."
        else -> "Algo deu errado: ${erro.message ?: "erro desconhecido"}"
    }
}
