# MVault Backend

API REST para gestão de estoque e custódia de ativos de alto valor. Construído em Java 25 com Spring Boot 4.1.2 e PostgreSQL.

## Sobre

O MVault é um ERP de controle patrimonial com rastreabilidade de movimentações, gestão de fornecedores e categorias. A API segue o padrão RESTful com autenticação JWT stateless e criptografia BCrypt.

### Stack

- **Java 25** (LTS)
- **Spring Boot 4.1.2**
- **Spring Security 6+** (JWT stateless com Auth0 java-jwt)
- **Spring Data JPA** (Hibernate 7 + JPA Specifications)
- **PostgreSQL**
- **Bean Validation** (Jakarta Validation + validador de CNPJ customizado)
- **Swagger / OpenAPI 3** (SpringDoc)

### Funcionalidades

- Autenticação JWT com BCrypt (endpoint `POST /login`)
- CRUD completo: Produtos, Categorias, Fornecedores e Movimentações
- Soft delete (inativação e reativação de registros sem perda de histórico)
- Validação customizada de CNPJ com anotação `@CNPJ`
- Filtros dinâmicos via JPA Specifications (busca, status ativo/inativo)
- Tratamento centralizado de exceções (`GlobalExceptionHandler`) com respostas padronizadas
- Controle de estoque com validação de saldo em movimentações de saída
- CORS configurado para integração com o frontend React

### Estrutura do Projeto

```
src/main/java/com/mvault/
├── config/          # Swagger, CORS
├── controller/      # Endpoints REST
├── dto/
│   ├── request/     # DTOs de entrada com validações
│   └── response/    # DTOs de saída
├── exception/       # Exceções customizadas e handler global
├── infra/           # Spring Security (JWT, filtros, configurações)
├── model/           # Entidades JPA
├── repository/      # Interfaces Spring Data
├── service/         # Regras de negócio
├── specification/   # Filtros dinâmicos JPA
└── validation/      # Validador de CNPJ customizado
```

## Como Executar

### Pré-requisitos

- Java 25
- PostgreSQL (com banco `mvault` criado)
- Variáveis de ambiente `JWT_SECRET_SECURITY` e `DB_PASSWORD` configuradas. Não há segredo padrão para JWT.

### Execução

```bash
# Via Maven Wrapper
./mvnw spring-boot:run

# Ou via IntelliJ IDEA (Run na classe MvaultBackendApplication)
```

A API roda na porta `8080` por padrão.

### Endpoints Principais

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| POST | `/login` | Autenticação (retorna JWT) |
| GET | `/produtos` | Listar produtos (paginado, filtrável) |
| POST | `/produtos` | Criar produto |
| PUT | `/produtos/{id}` | Atualizar produto |
| DELETE | `/produtos/{id}` | Inativar produto (soft delete) |
| GET | `/fornecedores?ativo=true` | Listar fornecedores ativos |
| PUT | `/fornecedores/{id}/reativar` | Reativar fornecedor |
| POST | `/movimentacoes` | Registrar entrada/saída de estoque |
| GET | `/categorias` | Listar categorias |

A documentação completa da API está disponível em `http://localhost:8080/swagger-ui.html` com o servidor rodando.

## Contexto de Desenvolvimento

Todo o código Java (entidades, serviços, controllers, segurança, validações e configurações) foi desenvolvido e debugado manualmente no IntelliJ IDEA. O frontend está em um repositório separado: [mvault-frontend](https://github.com/SEU_USUARIO/mvault-frontend)

## Testes

No Windows, execute `mvnw.cmd test`; em Linux/macOS, `./mvnw test`.
Os testes de contexto usam H2 em memória por configuração explícita e não acessam o PostgreSQL da aplicação. A suíte cobre autenticação, limites de entrada, expiração JWT e concorrência do estoque.

No IntelliJ, configure DB_PASSWORD e JWT_SECRET_SECURITY nas variáveis de ambiente da execução. Mantenha os valores na configuração local ignorada pelo Git; não os inclua no README, em arquivos compartilhados ou em commits.