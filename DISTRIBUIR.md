# Como distribuir o Zamypedia para o grupo

Guia para gerar o app instalável (APK) e mandar para as outras pessoas sem que elas percam os dados nas próximas versões.

## Por que isso importa

O Android só aceita instalar uma versão nova **por cima** da antiga, mantendo o jardim, quando:

1. as duas versões foram assinadas com a **mesma chave**;
2. a versão nova tem um número de versão maior (`versionCode` em `app/build.gradle.kts`, que o Claude aumenta a cada entrega);
3. o banco do celular é migrado sem apagar nada (o código já faz isso).

Se a chave se perder, ninguém consegue atualizar: a pessoa teria que desinstalar, e **desinstalar apaga o jardim**.

> Enquanto a sincronização com o Supabase não existir, os dados ficam só no celular. Trocar de aparelho, perder o celular ou desinstalar o app também apaga o jardim.

## 1. Criar a chave (uma única vez, só uma pessoa da equipe)

1. No Android Studio, menu **Build > Generate Signed App Bundle or APK...**
2. Escolha **APK** e clique em **Next**.
3. Em **Key store path**, clique em **Create new...**
   - **Key store path:** salve FORA da pasta do projeto, por exemplo `C:\Chaves\zamypedia.jks`.
   - **Password** (da chave e do arquivo): crie senhas fortes e anote no seu gerenciador de senhas.
   - **Alias:** `zamypedia`.
   - **Validity:** deixe 25 anos ou mais.
   - **Certificate:** basta preencher **First and Last Name** com `Zamypedia`. Os outros campos podem ficar vazios.
4. Clique em **OK**.

**Guarde duas cópias de segurança** do arquivo `zamypedia.jks` (por exemplo, um pen drive e uma pasta particular na nuvem), junto com as senhas num gerenciador de senhas. Nunca mande esse arquivo ao GitHub nem a quem não vai gerar versões. O `.gitignore` do projeto já bloqueia arquivos `.jks`, mas é bom não deixar a chave dentro da pasta do projeto.

## 2. Gerar o APK (a cada versão nova)

1. Confira que o `local.properties` tem o endereço e a chave pública do Supabase (o mesmo arquivo usado para rodar no celular).
2. **Build > Generate Signed App Bundle or APK... > APK > Next**.
3. Escolha o `zamypedia.jks`, digite as senhas e o alias `zamypedia`. Clique em **Next**.
4. Escolha **release** e clique em **Create**.
5. Quando terminar, clique em **locate** no aviso do canto da tela. O arquivo é `app\release\app-release.apk`.
6. Renomeie o arquivo para algo como `zamypedia-0.2.0.apk` (o número está em `versionName`, no `app/build.gradle.kts`).

## 3. Mandar e instalar

1. Mande o APK por WhatsApp ou por um link do Google Drive.
2. No celular, a pessoa toca no arquivo. Na primeira vez, o Android pede para **permitir instalar apps desta fonte** (do WhatsApp, do Drive ou do gerenciador de arquivos). Ela permite e volta.
3. O Google Play Protect pode avisar que o app é desconhecido. Toque em **Mais detalhes > Instalar mesmo assim**. Isso acontece porque o app não está na Play Store.
4. Ao abrir, aceitar as notificações para receber o aviso diário dos cuidados.

Nas versões seguintes, o mesmo processo mostra **Atualizar** em vez de Instalar, e o jardim continua lá.

## Atenção: quem já testou pelo Android Studio

O app instalado pelo botão **Run** usa uma chave de teste do computador, diferente da chave do passo 1. Para trocar para o APK assinado é preciso **desinstalar uma vez** (os dados de teste se perdem). Depois disso, use sempre o APK assinado. Se quiser testar pelo **Run** de novo, use outro celular ou desinstale antes.
