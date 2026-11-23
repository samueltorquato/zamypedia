// Arquivo: app/src/main/java/br/com/zamypedia/ui/conta/TelaCriarConta.kt
package br.com.zamypedia.ui.conta

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/** Tela 2: Criar conta. Pede só e-mail, @ e senha (decisão de 23/11/2026, LGPD). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCriarConta(vm: ContaViewModel, aoEntrar: () -> Unit, aoVoltar: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Criar conta") },
            navigationIcon = { TextButton(onClick = aoVoltar) { Text("Voltar") } },
        )
    }) { margens ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(margens)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            OutlinedTextField(
                value = vm.email,
                onValueChange = { vm.email = it },
                label = { Text("E-mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.arroba,
                onValueChange = vm::aoMudarArroba,
                label = { Text("Nome de usuário") },
                prefix = { Text("@") },
                singleLine = true,
                supportingText = { AvisoArroba(vm.situacaoArroba, vm.erroArroba) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = vm.senha,
                onValueChange = { vm.senha = it },
                label = { Text("Senha (mínimo 8 caracteres)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.confirmacaoSenha,
                onValueChange = { vm.confirmacaoSenha = it },
                label = { Text("Repita a senha") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            vm.mensagem?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.criarConta(aoEntrar) },
                enabled = !vm.carregando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (vm.carregando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Criar conta")
            }

            Spacer(Modifier.height(24.dp))
            // Texto curto de privacidade (LGPD): o que é guardado e onde
            Text(
                "Privacidade: guardamos só seu e-mail, seu @ e sua senha (protegida por criptografia), " +
                    "num serviço de nuvem (Supabase) na região de São Paulo. Suas plantas ficam no seu celular. " +
                    "As fotos são comprimidas e perdem a localização antes de serem salvas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AvisoArroba(situacao: SituacaoArroba, erro: String) {
    val (texto, cor) = when (situacao) {
        SituacaoArroba.VAZIO -> "Letras minúsculas, números, ponto ou sublinhado" to Color.Unspecified
        SituacaoArroba.INVALIDO -> "Use de 3 a 20 caracteres: a-z, 0-9, ponto ou sublinhado" to MaterialTheme.colorScheme.error
        SituacaoArroba.CONFERINDO -> "Conferindo..." to Color.Unspecified
        SituacaoArroba.DISPONIVEL -> "Disponível" to MaterialTheme.colorScheme.primary
        SituacaoArroba.EM_USO -> "Já está em uso" to MaterialTheme.colorScheme.error
        SituacaoArroba.ERRO -> erro to MaterialTheme.colorScheme.error
    }
    Text(texto, color = cor)
}
