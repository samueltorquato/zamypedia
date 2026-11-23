// Arquivo: app/src/main/java/br/com/zamypedia/ZamypediaApp.kt
package br.com.zamypedia

import android.app.Application
import br.com.zamypedia.dados.JardimRepositorio
import br.com.zamypedia.dados.local.BancoLocal
import br.com.zamypedia.dados.local.SessaoLocal
import br.com.zamypedia.dados.nuvem.ContaRepositorio
import br.com.zamypedia.dados.nuvem.Supabase

/**
 * Criado uma única vez quando o app abre. Guarda as peças compartilhadas por todas as telas
 * (banco, sessão, conexão com o Supabase), para não criar várias cópias delas.
 */
class ZamypediaApp : Application() {
    lateinit var sessao: SessaoLocal
        private set
    // Criada só quando a primeira tela de conta precisar. Se o local.properties não tiver
    // o endereço do Supabase, o erro aparece na tela de login em vez de fechar o app.
    val conta: ContaRepositorio by lazy { ContaRepositorio(Supabase.criarCliente(), sessao) }
    lateinit var jardim: JardimRepositorio
        private set

    override fun onCreate() {
        super.onCreate()
        sessao = SessaoLocal(this)
        jardim = JardimRepositorio(BancoLocal.criar(this).plantaJardimDao())
    }
}
