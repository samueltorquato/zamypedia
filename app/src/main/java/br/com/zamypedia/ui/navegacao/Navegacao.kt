// Arquivo: app/src/main/java/br/com/zamypedia/ui/navegacao/Navegacao.kt
package br.com.zamypedia.ui.navegacao

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.zamypedia.ZamypediaApp
import br.com.zamypedia.ui.comum.TelaEmBreve
import br.com.zamypedia.ui.conta.ContaViewModel
import br.com.zamypedia.ui.conta.TelaCriarConta
import br.com.zamypedia.ui.conta.TelaEntrar
import br.com.zamypedia.ui.jardim.EdicaoPlantaViewModel
import br.com.zamypedia.ui.jardim.JardimViewModel
import br.com.zamypedia.ui.jardim.TelaAdicionarPlanta
import br.com.zamypedia.ui.jardim.TelaMeuJardim
import br.com.zamypedia.ui.jardim.TelaPlanta
import kotlinx.coroutines.launch

/** Nomes das telas (rotas). */
private object Rotas {
    const val ENTRAR = "entrar"
    const val CRIAR_CONTA = "criar_conta"
    const val PERFIL = "perfil"
    const val JARDIM = "jardim"
    const val LINHA_DO_TEMPO = "linha_do_tempo"
    const val OS_JARDINS = "os_jardins"
    const val ADICIONAR = "adicionar"
    const val EDITAR = "editar/{id}"
    const val PLANTA = "planta/{id}"
}

/** As quatro abas da barra inferior, com os emojis pedidos no documento de requisitos. */
private enum class Aba(val rota: String, val emoji: String, val rotulo: String) {
    PERFIL(Rotas.PERFIL, "🏠", "Perfil"),
    JARDIM(Rotas.JARDIM, "🪴", "Meu Jardim"),
    LINHA(Rotas.LINHA_DO_TEMPO, "📋", "Linha do tempo"),
    OS_JARDINS(Rotas.OS_JARDINS, "🌱", "Os Jardins"),
}

@Composable
fun Navegacao(app: ZamypediaApp) {
    val nav = rememberNavController()
    val entradaAtual by nav.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route
    // A barra inferior só aparece nas quatro abas principais
    val mostrarBarra = Aba.entries.any { it.rota == rotaAtual }

    // Se já houve login neste aparelho, abre direto no Meu Jardim (funciona sem internet)
    // "remember" fixa a escolha: a tela inicial não deve mudar depois que o app já abriu
    val inicio = remember { if (app.sessao.estaLogado) Rotas.JARDIM else Rotas.ENTRAR }

    // As margens do topo (barra de status) ficam a cargo de cada tela, por isso zeramos aqui
    Scaffold(
        bottomBar = { if (mostrarBarra) BarraInferior(nav, rotaAtual) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { margens ->
        NavHost(nav, startDestination = inicio, modifier = Modifier.padding(margens)) {

            composable(Rotas.ENTRAR) {
                val vm: ContaViewModel = viewModel(factory = viewModelFactory { initializer { ContaViewModel(app) } })
                TelaEntrar(vm, aoEntrar = { irParaJardimLimpandoHistorico(nav) }, aoCriarConta = { nav.navigate(Rotas.CRIAR_CONTA) })
            }

            composable(Rotas.CRIAR_CONTA) {
                // Reaproveita o mesmo ViewModel da tela Entrar, para o e-mail digitado não se perder
                val entradaEntrar = remember(it) { nav.getBackStackEntry(Rotas.ENTRAR) }
                val vm: ContaViewModel = viewModel(
                    viewModelStoreOwner = entradaEntrar,
                    factory = viewModelFactory { initializer { ContaViewModel(app) } },
                )
                TelaCriarConta(vm, aoEntrar = { irParaJardimLimpandoHistorico(nav) }, aoVoltar = { nav.popBackStack() })
            }

            composable(Rotas.JARDIM) {
                val donoId = app.sessao.usuarioId ?: return@composable
                val vm: JardimViewModel = viewModel(factory = viewModelFactory { initializer { JardimViewModel(app.jardim, donoId) } })
                TelaMeuJardim(
                    vm,
                    aoAdicionar = { nav.navigate(Rotas.ADICIONAR) },
                    aoAbrirPlanta = { id -> nav.navigate("planta/$id") },
                )
            }

            composable(Rotas.ADICIONAR) {
                val donoId = app.sessao.usuarioId ?: return@composable
                val vm: EdicaoPlantaViewModel = viewModel(
                    factory = viewModelFactory { initializer { EdicaoPlantaViewModel(app.jardim, donoId, null) } },
                )
                TelaAdicionarPlanta(vm, aoTerminar = { nav.popBackStack() })
            }

            composable(Rotas.EDITAR, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entrada ->
                val donoId = app.sessao.usuarioId ?: return@composable
                val id = entrada.arguments?.getString("id") ?: return@composable
                val vm: EdicaoPlantaViewModel = viewModel(
                    factory = viewModelFactory { initializer { EdicaoPlantaViewModel(app.jardim, donoId, id) } },
                )
                TelaAdicionarPlanta(vm, aoTerminar = { nav.popBackStack() })
            }

            composable(Rotas.PLANTA, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entrada ->
                val id = entrada.arguments?.getString("id") ?: return@composable
                TelaPlanta(
                    app.jardim,
                    id,
                    aoVoltar = { nav.popBackStack() },
                    aoEditar = { nav.navigate("editar/$it") },
                )
            }

            composable(Rotas.PERFIL) {
                val escopo = rememberCoroutineScope()
                TelaEmBreve(
                    "🏠",
                    "@${app.sessao.arroba.orEmpty()}",
                    "Seu perfil, com minibio e plantômetro, chega numa próxima entrega.",
                ) {
                    OutlinedButton(onClick = {
                        escopo.launch {
                            app.conta.sair()
                            nav.navigate(Rotas.ENTRAR) { popUpTo(nav.graph.id) { inclusive = true } }
                        }
                    }) { Text("Sair da conta") }
                }
            }

            composable(Rotas.LINHA_DO_TEMPO) {
                TelaEmBreve("📋", "Linha do tempo", "Aqui vão aparecer suas plantas adicionadas e edições do jardim.")
            }

            composable(Rotas.OS_JARDINS) {
                TelaEmBreve("🌱", "Os Jardins", "O catálogo com fichas oficiais e anotações compartilhadas chega numa próxima entrega.")
            }
        }
    }
}

@Composable
private fun BarraInferior(nav: NavHostController, rotaAtual: String?) {
    NavigationBar {
        Aba.entries.forEach { aba ->
            NavigationBarItem(
                selected = rotaAtual == aba.rota,
                onClick = {
                    nav.navigate(aba.rota) {
                        // Evita acumular várias cópias da mesma aba ao trocar de aba
                        popUpTo(Rotas.JARDIM) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Text(aba.emoji, fontSize = 22.sp) },
                label = { Text(aba.rotulo, fontSize = 11.sp) },
            )
        }
    }
}

/** Depois de entrar, o Meu Jardim vira a primeira tela e o botão Voltar não leva de novo ao login. */
private fun irParaJardimLimpandoHistorico(nav: NavHostController) {
    nav.navigate(Rotas.JARDIM) { popUpTo(nav.graph.id) { inclusive = true } }
}
