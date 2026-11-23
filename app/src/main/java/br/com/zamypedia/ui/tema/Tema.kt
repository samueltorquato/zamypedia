// Arquivo: app/src/main/java/br/com/zamypedia/ui/tema/Tema.kt
package br.com.zamypedia.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Aplica a paleta do Zamypedia aos componentes do Material 3 (botões, campos, barras). */
private val esquemaClaro = lightColorScheme(
    primary = VerdeOliva,
    onPrimary = Color.White,
    primaryContainer = VerdeSalvia,
    onPrimaryContainer = VerdeEscuro,
    secondary = VerdeSalvia,
    onSecondary = VerdeEscuro,
    secondaryContainer = Champagne,
    onSecondaryContainer = VerdeEscuro,
    tertiary = Champagne,
    background = Marfim,
    onBackground = TextoPrincipal,
    surface = Marfim,
    onSurface = TextoPrincipal,
    surfaceVariant = Champagne,
    onSurfaceVariant = VerdeEscuro,
    surfaceContainer = Champagne,
    error = ErroSuave,
)

@Composable
fun TemaZamypedia(conteudo: @Composable () -> Unit) {
    MaterialTheme(colorScheme = esquemaClaro, content = conteudo)
}
