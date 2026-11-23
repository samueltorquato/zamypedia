// Arquivo: app/src/main/java/br/com/zamypedia/ui/conta/ContaViewModel.kt
package br.com.zamypedia.ui.conta

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.zamypedia.ZamypediaApp
import br.com.zamypedia.dados.nuvem.ResultadoCadastro
import br.com.zamypedia.dados.nuvem.mensagemDeErro
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Situação do @ digitado no cadastro, mostrada logo abaixo do campo. */
enum class SituacaoArroba { VAZIO, INVALIDO, CONFERINDO, DISPONIVEL, EM_USO, SEM_CONEXAO }

/**
 * Guarda o que foi digitado nas telas Entrar e Criar conta e conversa com o Supabase.
 * A tela só mostra os valores daqui. Assim nada se perde se o celular girar.
 */
class ContaViewModel(private val app: ZamypediaApp) : ViewModel() {

    var email by mutableStateOf("")
    var senha by mutableStateOf("")
    var confirmacaoSenha by mutableStateOf("")
    var arroba by mutableStateOf("")
        private set
    var situacaoArroba by mutableStateOf(SituacaoArroba.VAZIO)
        private set
    var carregando by mutableStateOf(false)
        private set
    var mensagem by mutableStateOf<String?>(null)
        private set

    private var conferenciaArroba: Job? = null

    /** Chamado a cada letra digitada no campo @. Espera meio segundo parado antes de consultar. */
    fun aoMudarArroba(texto: String) {
        // Remove o "@" se a pessoa digitar, e deixa tudo minúsculo
        arroba = texto.removePrefix("@").lowercase().trim()
        conferenciaArroba?.cancel()
        situacaoArroba = when {
            arroba.isEmpty() -> SituacaoArroba.VAZIO
            !Regex("^[a-z0-9_.]{3,20}$").matches(arroba) -> SituacaoArroba.INVALIDO
            else -> SituacaoArroba.CONFERINDO
        }
        if (situacaoArroba != SituacaoArroba.CONFERINDO) return
        val pedido = arroba
        conferenciaArroba = viewModelScope.launch {
            delay(500)
            situacaoArroba = try {
                if (app.conta.arrobaDisponivel(pedido)) SituacaoArroba.DISPONIVEL else SituacaoArroba.EM_USO
            } catch (e: Exception) {
                SituacaoArroba.SEM_CONEXAO
            }
        }
    }

    fun limparMensagem() {
        mensagem = null
    }

    fun entrar(aoEntrar: () -> Unit) {
        if (email.isBlank() || senha.isBlank()) {
            mensagem = "Preencha e-mail e senha."
            return
        }
        executar {
            app.conta.entrar(email.trim(), senha)
            aoEntrar()
        }
    }

    fun criarConta(aoEntrar: () -> Unit) {
        mensagem = when {
            !email.contains("@") || !email.contains(".") -> "Digite um e-mail válido."
            situacaoArroba == SituacaoArroba.INVALIDO || arroba.isEmpty() ->
                "O @ precisa ter de 3 a 20 caracteres: letras minúsculas, números, ponto ou sublinhado."
            situacaoArroba == SituacaoArroba.EM_USO -> "Este @ já está em uso. Escolha outro."
            senha.length < 8 -> "A senha precisa ter pelo menos 8 caracteres."
            senha != confirmacaoSenha -> "As senhas não são iguais."
            else -> null
        }
        if (mensagem != null) return
        executar {
            when (app.conta.criarConta(email.trim(), senha, arroba)) {
                ResultadoCadastro.CONTA_CRIADA_E_LOGADA -> aoEntrar()
                ResultadoCadastro.CONFIRMAR_EMAIL -> mensagem =
                    "Conta criada! Enviamos um link para ${email.trim()}. Confirme o e-mail e depois entre."
            }
        }
    }

    /** Roda uma operação de rede mostrando "carregando" e traduzindo erros. */
    private fun executar(bloco: suspend () -> Unit) {
        viewModelScope.launch {
            carregando = true
            mensagem = null
            try {
                bloco()
            } catch (e: Exception) {
                mensagem = mensagemDeErro(e)
            } finally {
                carregando = false
            }
        }
    }
}
