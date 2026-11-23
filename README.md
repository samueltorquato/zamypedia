# Zamypedia

App Android de cultivo de plantas domésticas, para uso pessoal e de pequenos grupos, sem fins comerciais.

**Pilha técnica (aprovada em 23/11/2026):** Kotlin, Jetpack Compose, Room (banco no celular), Supabase no plano gratuito (contas e, depois, sincronização e catálogo).

## O que já funciona (primeira entrega)

- Criar conta (e-mail, @ e senha) com aviso de @ disponível, e entrar.
- Barra inferior com as quatro abas: Perfil, Meu Jardim, Linha do tempo e Os Jardins. Perfil, Linha do tempo e Os Jardins ainda mostram "em breve".
- Meu Jardim: grade de três colunas, filtro Tenho, Já tive e Quero ter, e três formas de ordenar.
- Adicionar, editar e excluir planta, com foto da câmera ou da galeria. A foto é comprimida (WebP, até 1280 px) e perde a localização GPS.
- Depois do primeiro login, o jardim funciona sem internet. Nesta entrega as plantas ficam só no celular: desinstalar o app apaga o jardim.

## O que chega na segunda entrega

- Ficha da planta com três abas: **Informações** (Bloco 01: taxonomia, luz, água, solo, nutrientes, sinais nas folhas, pragas e doenças), **História** (Bloco 02: desde quando está em casa, com só o ano obrigatório, situação de floração e datas em que floresceu) e **Cuidados** (Bloco 03: Reguei, Adubei, Podei, Troquei de vaso e Limpei as folhas, com a data de hoje ou outra escolhida no calendário, e histórico com opção de apagar).
- Lembretes que aprendem o ritmo de cada cuidado: com dois registros ou mais, o app calcula o intervalo médio das últimas 5 datas e prevê a próxima. Com menos de dois registros não há lembrete.
- Um único aviso por dia, às 8h, juntando todos os cuidados que vencem. Funciona sem internet.
- Configurações (engrenagem no Perfil): ligar ou desligar cada tipo de lembrete, mudar o horário do aviso e sair da conta.
- O banco do celular passa para a versão 2. As plantas da primeira entrega são mantidas.
- Testes do cálculo dos lembretes em `app/src/test`. No Android Studio, clique com o botão direito em `CalculoLembretesTest` e escolha **Run**.

## Como rodar pela primeira vez

### 1. Supabase (uma vez só, para a equipe toda)

1. Crie uma conta gratuita em https://supabase.com (não é preciso cartão).
2. Clique em **New project**. Em **Region**, escolha **South America (São Paulo)**. Anote a senha do banco num lugar seguro.
3. No menu lateral, abra **SQL Editor**, cole todo o arquivo `supabase/001_perfil.sql` e clique em **Run**.
4. Em **Project Settings > API**, copie o **Project URL** e a chave **anon public**. Nunca use nem compartilhe a chave **service_role**.
5. Opcional: em **Authentication > Sign In / Providers > Email**, a opção **Confirm email** vem ligada. Com ela ligada, quem cria a conta recebe um e-mail e só consegue entrar depois de confirmar. Para um grupo pequeno pode ficar ligada.

### 2. Android Studio (em cada computador)

1. Instale o Android Studio (gratuito): https://developer.android.com/studio
2. **File > Open** e escolha esta pasta. Espere o Android Studio baixar o que precisa (a primeira vez demora).
3. Abra o arquivo `local.properties` na raiz do projeto (o Android Studio cria sozinho) e acrescente as duas linhas de `local.properties.exemplo`, com o endereço e a chave anon do passo 1.4. Esse arquivo não vai para o GitHub.
4. Conecte um celular Android 8 ou mais novo com a **Depuração USB** ligada, ou crie um emulador em **Device Manager**, e clique em **Run** (▶).

## Organização das pastas

| Pasta | O que tem |
| --- | --- |
| `app/src/main/java/br/com/zamypedia/ui/` | Telas (conta, jardim, navegação, tema) |
| `app/src/main/java/br/com/zamypedia/dados/local/` | Banco do celular (Room) e sessão |
| `app/src/main/java/br/com/zamypedia/dados/nuvem/` | Conexão com o Supabase |
| `app/src/main/java/br/com/zamypedia/lembretes/` | Cálculo dos lembretes e aviso diário |
| `app/src/main/java/br/com/zamypedia/util/Fotos.kt` | Compressão das fotos |
| `supabase/` | Scripts SQL para colar no painel do Supabase |

## Segurança e privacidade

- A chave anon fica no app, o que é normal. Quem protege os dados são as regras de segurança (RLS) do banco.
- Coletamos só e-mail, @ e senha. A senha é guardada criptografada pelo Supabase.
- As fotos não guardam a localização de onde foram tiradas.
