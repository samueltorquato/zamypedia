// Arquivo: app/src/main/java/br/com/zamypedia/util/Fotos.kt
package br.com.zamypedia.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.util.UUID
import kotlin.math.max

/**
 * Prepara as fotos das plantas para ocupar pouco espaço.
 *
 * O que acontece com cada foto:
 * 1. É lida em tamanho reduzido (economiza memória do celular).
 * 2. É girada para ficar em pé, conforme a orientação gravada pela câmera.
 * 3. É reduzida para no máximo 1280 pixels no lado maior.
 * 4. É salva em WebP com qualidade 75, ficando em geral entre 150 e 250 KB.
 *
 * Como a imagem é recriada do zero, os metadados da foto original (EXIF),
 * inclusive a localização GPS, NÃO vão para o arquivo final. Isso protege a privacidade.
 */
object Fotos {
    private const val LADO_MAXIMO = 1280
    private const val QUALIDADE = 75

    /** Comprime a foto indicada por [origem] e devolve o caminho do novo arquivo. Rode fora da tela principal. */
    fun comprimirESalvar(context: Context, origem: Uri): String {
        val resolver = context.contentResolver

        // 1. Descobre o tamanho original sem carregar a imagem inteira
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(origem).use { BitmapFactory.decodeStream(it, null, limites) }
        var fator = 1
        while (max(limites.outWidth, limites.outHeight) / (fator * 2) >= LADO_MAXIMO) fator *= 2

        // 2. Carrega já reduzida pelo fator calculado
        val opcoes = BitmapFactory.Options().apply { inSampleSize = fator }
        val original = resolver.openInputStream(origem).use { BitmapFactory.decodeStream(it, null, opcoes) }
            ?: error("Não foi possível ler a foto.")

        // 3. Corrige a rotação e reduz ao tamanho final
        val rotacao = resolver.openInputStream(origem).use { entrada ->
            if (entrada == null) 0 else when (
                ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
        val escala = minOf(1f, LADO_MAXIMO.toFloat() / max(original.width, original.height))
        val matriz = Matrix().apply {
            postScale(escala, escala)
            postRotate(rotacao.toFloat())
        }
        val final = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)

        // 4. Salva em WebP na pasta privada do app (outros apps não enxergam)
        val pasta = File(context.filesDir, "fotos").apply { mkdirs() }
        val arquivo = File(pasta, "${UUID.randomUUID()}.webp")
        val formato = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        }
        arquivo.outputStream().use { final.compress(formato, QUALIDADE, it) }

        if (final !== original) final.recycle()
        original.recycle()
        return arquivo.absolutePath
    }

    /** Arquivo temporário onde o app de câmera grava a foto antes da compressão. */
    fun arquivoTemporarioDaCamera(context: Context): File {
        val pasta = File(context.cacheDir, "camera").apply { mkdirs() }
        return File(pasta, "foto_temporaria.jpg")
    }

    /** Apaga uma foto que não é mais usada. */
    fun apagar(caminho: String?) {
        if (caminho != null) runCatching { File(caminho).delete() }
    }
}
