-- Arquivo: supabase/001_perfil.sql
-- Como usar: no painel do Supabase, abra "SQL Editor", cole todo este arquivo e clique em "Run".
-- Rode uma única vez, logo depois de criar o projeto.
--
-- O que este script cria:
-- 1. A tabela "perfil", com o @ de cada pessoa (sem repetição).
-- 2. Regras de segurança (RLS): cada pessoa só enxerga o próprio perfil.
-- 3. Um gatilho que cria o perfil automaticamente quando uma conta nova é criada pelo app.
-- 4. Uma função que responde apenas "sim" ou "não" para "este @ está livre?",
--    sem permitir que alguém liste os usuários.

-- 1. Tabela de perfis
create table public.perfil (
  id uuid primary key references auth.users (id) on delete cascade,
  arroba text not null unique check (arroba ~ '^[a-z0-9_.]{3,20}$'),
  criado_em timestamptz not null default now()
);

-- 2. Segurança: com RLS ligado, nada é visível a não ser o que as regras abaixo liberam
alter table public.perfil enable row level security;

create policy "cada pessoa le o proprio perfil"
  on public.perfil for select
  to authenticated
  using ((select auth.uid()) = id);

-- 3. Gatilho: ao criar uma conta, copia o @ enviado pelo app para a tabela perfil.
--    Se o @ já existir, a criação da conta inteira é cancelada (o app avisa a pessoa).
create or replace function public.criar_perfil_da_conta_nova()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.perfil (id, arroba)
  values (new.id, lower(new.raw_user_meta_data ->> 'arroba'));
  return new;
end;
$$;

create trigger ao_criar_conta
  after insert on auth.users
  for each row execute function public.criar_perfil_da_conta_nova();

-- 4. Consulta de @ disponível, usada pela tela Criar conta enquanto a pessoa digita
create or replace function public.arroba_disponivel(p_arroba text)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select not exists (select 1 from public.perfil where arroba = lower(p_arroba));
$$;

revoke all on function public.arroba_disponivel(text) from public;
grant execute on function public.arroba_disponivel(text) to anon, authenticated;
