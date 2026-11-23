// Arquivo: app/src/main/java/br/com/zamypedia/MainActivity.kt
package br.com.zamypedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.zamypedia.ui.navegacao.Navegacao
import br.com.zamypedia.ui.tema.TemaZamypedia

/** Ponto de entrada do app: aplica o tema e mostra a navegação entre as telas. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ZamypediaApp
        setContent {
            TemaZamypedia {
                Navegacao(app)
            }
        }
    }
}
