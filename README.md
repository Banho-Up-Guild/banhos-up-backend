# BanhoUP - Backend

API REST do sistema de petshop BanhoUP, feita com Java 21, Spring Boot e Maven.
Banco de dados: PostgreSQL (Neon).

## Como rodar

1. Abra o projeto no IntelliJ e aguarde o Maven carregar.
2. Confira o `application.properties` (banco e `jwt.secret`).
3. Rode a classe `BackendApplication`.
4. Abra o Swagger: http://localhost:8080/swagger-ui.html

## Estrutura

- `controller`: recebe as requisições
- `service`: regras de negócio
- `repository`: acesso ao banco
- `model`: entidades e enums
- `dto`: formato dos JSONs
- `security`: JWT
- `config`: segurança e Swagger
- `common/exception`: tratamento de erros

## Perfis de acesso

| Perfil | O que pode |
|---|---|
| CUSTOMER | Se cadastra sozinho e vê os próprios dados |
| EMPLOYEE | Criado pelo admin |
| ADMIN | Cria funcionários e gerencia usuários |

## Rotas de usuário

| Rota | Quem acessa |
|---|---|
| `POST /users/register` | Todos |
| `POST /auth/login` | Todos |
| `GET /users/me` | Qualquer pessoa logada |
| `POST /users/staff` | Admin |
| `GET /users` | Admin |
| `PUT /users/{id}` | Admin |
| `PATCH /users/{id}/active` | Admin |

## Como usar o token no Swagger

1. Faça `POST /auth/login` e copie o `token`.
2. Clique em **Authorize**, cole o token e confirme.

## Primeiro administrador

Cadastre um usuário normal e mude o perfil direto no banco:

    UPDATE users SET role = 'ADMIN' WHERE email = 'seu.email@teste.com';

Depois faça login de novo para gerar um token com o perfil novo.
