<div align="center">

# 💰 Budgeting API
### Controle de Gastos Inteligente com Spring Boot + IA

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-9.6-blue?logo=mysql)](https://www.mysql.com/)
[![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o--mini-412991?logo=openai)](https://openai.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<p>
  <strong>API REST para registro e consulta de transações financeiras,</strong><br>
  com processamento de áudio via Inteligência Artificial (Whisper + GPT-4o-mini).
</p>

[🚀 Como Executar](#-como-executar) • [📡 Endpoints](#-endpoints) • [🧪 Testes](#-testes) • [📚 Aprendizados](#-o-que-aprendi)

</div>

---

## 📖 Índice

- [O que o projeto faz](#-o-que-o-projeto-faz)
- [Tecnologias utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
- [Como executar](#-como-executar)
- [Endpoints da API](#-endpoints-da-api)
- [Como testar o fluxo principal](#-como-testar-o-fluxo-principal)
- [Melhorias implementadas](#-melhorias-implementadas)
- [O que aprendi](#-o-que-aprendi)
- [Estrutura de pastas](#-estrutura-de-pastas)

---

## ✨ O que o projeto faz

A **Budgeting API** é uma aplicação backend que permite:

| Funcionalidade | Descrição |
|----------------|-----------|
| 📝 **Cadastrar gastos** | Registrar transações financeiras com descrição, valor e categoria. |
| 🔍 **Consultar por categoria** | Listar todos os gastos de uma categoria específica (`GROCERIES`, `PHARMA`, `AUTO`). |
| 🎙️ **Processar áudio** | Enviar um áudio descrevendo um gasto (ex: *"Gastei 50 reais na farmácia"*) e a IA extrai automaticamente os dados, persiste no banco e retorna uma resposta em áudio. |
| 📊 **Resumo financeiro** | Obter um consolidado dos gastos por categoria. |

> 💡 **Diferencial:** A integração com **Spring AI** permite interação por voz, tornando o registro de gastos mais natural e acessível.

---

## 🛠 Tecnologias utilizadas

| Camada | Tecnologia | Versão | Função |
|--------|-----------|--------|--------|
| **Linguagem** | Java | 25 | Lógica de negócio e API |
| **Framework** | Spring Boot | 4.0.5 | Estrutura da aplicação |
| **Persistência** | Spring Data JPA + Hibernate | 7.2.7 | Acesso e mapeamento do banco |
| **Banco de dados** | MySQL | 9.6 | Armazenamento das transações |
| **IA / LLM** | Spring AI + OpenAI | — | Chat (GPT-4o-mini), Transcrição (Whisper), Voz (TTS) |
| **Container** | Docker Compose | — | Orquestração do MySQL |
| **Build** | Gradle | — | Gerenciamento de dependências |
| **Utilitários** | Lombok | — | Redução de boilerplate |

---

## 🏗 Arquitetura

O projeto segue uma arquitetura em camadas com **Clean Architecture** simplificada:

```
┌─────────────────────────────────────────────┐
│  🌐 HTTP (TransactionController)            │  ← REST API
├─────────────────────────────────────────────┤
│  ⚙️  Application (Use Cases)                 │  ← Regras de negócio
├─────────────────────────────────────────────┤
│  🧠 Domain (Entities, Enums, Repository)      │  ← Modelos puros
├─────────────────────────────────────────────┤
│  💾 Infrastructure (JPA, HTTP, AI)           │  ← Adaptadores
└─────────────────────────────────────────────┘
```

**Fluxo de processamento de áudio:**
```
Usuário envia áudio MP3
        ↓
[Whisper] Transcreve para texto
        ↓
[GPT-4o-mini + Tools] Interpreta e chama a função de persistência
        ↓
[JPA + MySQL] Salva a transação
        ↓
[TTS] Gera resposta em áudio MP3
        ↓
Retorna áudio ao usuário
```

---

## 🚀 Como executar

### Pré-requisitos

- [Git](https://git-scm.com/)
- [Java 25](https://openjdk.org/projects/jdk/25/)
- [Docker](https://www.docker.com/) (para o MySQL)
- Chave de API da [OpenAI](https://platform.openai.com/api-keys)

### Passo a passo

#### 1. Clone o repositório

```bash
git clone https://github.com/john-richard13/dio-spring-boot-learning-track.git
cd dio-spring-boot-learning-track/05-spring-ai
```

#### 2. Configure a chave da OpenAI

```bash
export OPENAI_API_KEY="sua-chave-aqui"
```

> 💡 **Dica:** No GitHub Codespaces, você pode adicionar a chave em `Settings > Secrets and variables > Codespaces`.

#### 3. Execute a aplicação

```bash
./gradlew bootRun
```

O que acontece automaticamente:
- ✅ O Spring Boot detecta o `compose.yml` e sobe o container MySQL na porta `3307`.
- ✅ O banco `transaction` é criado com as credenciais definidas no Docker Compose.
- ✅ As tabelas são geradas automaticamente pelo Hibernate (`ddl-auto=update`).
- ✅ A API fica disponível em `http://localhost:8080`.

> 🐳 **Nota:** O MySQL roda via Docker Compose. Não é necessário instalá-lo localmente.

---

## 📡 Endpoints da API

### Base URL
```
http://localhost:8080
```

### 1. Criar uma transação

```http
POST /transactions
Content-Type: application/json
```

**Request body:**
```json
{
  "description": "Compra no mercado",
  "amount": 15050,
  "category": "GROCERIES"
}
```

> ⚠️ O campo `amount` é em **centavos** (inteiro). Ex: R$ 150,50 = `15050`.

**Response `201 Created`:**
```json
{
  "id": "f85182cf-8bb0-46e5-9b62-7fb43bc2ccb3",
  "category": "GROCERIES",
  "description": "Compra no mercado",
  "amount": 15050.0
}
```

**Exemplo com `curl`:**
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Compra no mercado","amount":15050,"category":"GROCERIES"}'
```

---

### 2. Listar transações por categoria

```http
GET /transactions/{category}
```

**Categorias disponíveis:** `GROCERIES` | `PHARMA` | `AUTO`

**Exemplo com `curl`:**
```bash
curl http://localhost:8080/transactions/GROCERIES
```

**Response `200 OK`:**
```json
[
  {
    "id": "f85182cf-8bb0-46e5-9b62-7fb43bc2ccb3",
    "category": "GROCERIES",
    "description": "Compra no mercado",
    "amount": 15050.0
  }
]
```

---

### 3. Processar áudio com IA

```http
POST /transactions/ai
Content-Type: multipart/form-data
```

**Parâmetro:** `file` (arquivo de áudio `.mp3` ou `.wav`)

**Exemplo com `curl`:**
```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@/caminho/do/audio.mp3" \
  --output resposta.mp3
```

**Comportamento:**
1. Transcreve o áudio com **Whisper** (PT-BR).
2. A IA interpreta o texto e extrai: descrição, valor e categoria.
3. Persiste a transação no MySQL.
4. Retorna um **áudio MP3** com a confirmação do registro.

> 🎙️ **Prompt customizado:** O `application.properties` inclui um prompt específico para reconhecimento de gastos em português brasileiro.

---

## 🧪 Como testar o fluxo principal

### Teste 1: Fluxo completo via JSON

```bash
# 1. Criar transação
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Remédio na farmácia","amount":4590,"category":"PHARMA"}'

# 2. Criar outra transação
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Abastecimento","amount":20000,"category":"AUTO"}'

# 3. Consultar todas as categorias
curl http://localhost:8080/transactions/GROCERIES
curl http://localhost:8080/transactions/PHARMA
curl http://localhost:8080/transactions/AUTO
```

### Teste 2: Verificar persistência no banco

```bash
# Acesse o MySQL dentro do container
docker exec -it 05-spring-ai-database-1 mysql -u app -papp -e "SELECT * FROM transaction.transaction;"
```

### Teste 3: Build limpo

```bash
./gradlew clean build
```

Deve retornar `BUILD SUCCESSFUL`.

### Teste 4: Testes automatizados

```bash
./gradlew test
```

---

## 🚀 Melhorias implementadas

Durante o desenvolvimento, foram aplicadas as seguintes melhorias:

| Melhoria | Descrição |
|----------|-----------|
| 🔧 **Configuração explícita do DataSource** | Adicionado `spring.datasource.*` no `application.properties` com fallback via variáveis de ambiente, garantindo funcionamento mesmo sem Docker Compose Support. |
| 🐳 **Docker Compose nativo do Spring Boot 4** | O `compose.yml` é detectado automaticamente, eliminando a necessidade de `docker compose up` manual. |
| 🧹 **Limpeza do repositório** | Remoção de arquivos temporários e configuração adequada do `.gitignore`. |
| 🧪 **Testes de unidade** | Adicionado `GetFinancialSummaryUseCaseTest` para validar a lógica de resumo financeiro. |
| 🎙️ **Prompt customizado para PT-BR** | O prompt de transcrição foi ajustado para reconhecer padrões de gastos em português brasileiro ("reais", "gastei", "comprei", etc.). |
| 🔇 **Supressão de warnings** | Configuração `spring.jpa.open-in-view=false` para eliminar warnings do Hibernate. |

---

## 📚 O que aprendi

> Este projeto foi desenvolvido durante o **Bootcamp Santander Backend Java** da [DIO](https://www.dio.me/).

### Conceitos consolidados

- **Spring Boot 4.0:** Uso do Docker Compose Support nativo, que simplifica drasticamente o desenvolvimento local com bancos de dados.
- **Spring AI:** Integração com múltiplos modelos da OpenAI (Chat, Transcrição e TTS) usando uma abstração unificada.
- **Pattern Tool Calling:** A IA não apenas responde textos — ela **invoca métodos Java** (`@Tool`) para persistir dados, criando uma interação bidirecional.
- **Arquitetura em Camadas:** Separação clara entre Domain, Application e Infrastructure, facilitando testes e manutenção.
- **Docker no dia a dia:** O `compose.yml` como parte do código-fonte, garantindo que qualquer pessoa rode o projeto com um único comando.

### Desafios enfrentados

| Desafio | Solução |
|---------|---------|
| `Failed to configure a DataSource` no Codespaces | Entendi que o Spring Boot 4 lê o `compose.yml` automaticamente, mas deixei a configuração explícita como fallback. |
| Conflito de branches no Git | Aprendi a usar `git pull --no-rebase` para sincronizar mudanças do repositório remoto antes do push. |
| Porta do MySQL | O `compose.yml` mapeia `3307:3306`, então a aplicação conecta em `localhost:3307` enquanto o container interno usa `3306`. |

---

## 📁 Estrutura de pastas

```
05-spring-ai/
├── compose.yml                          # Docker Compose (MySQL 9.6)
├── build.gradle                         # Dependências Gradle
├── src/
│   ├── main/
│   │   ├── java/dio/budgeting/
│   │   │   ├── application/             # Casos de uso
│   │   │   │   ├── GetFinancialSummaryUseCase.java
│   │   │   │   ├── ListTransactionsByCategoryUseCase.java
│   │   │   │   ├── PersistTransactionUseCase.java
│   │   │   │   ├── input/
│   │   │   │   │   └── PersistTransactionInput.java
│   │   │   │   └── output/
│   │   │   │       ├── FinancialSummaryOutput.java
│   │   │   │       └── TransactionOutput.java
│   │   │   ├── domain/                  # Entidades e regras
│   │   │   │   ├── Category.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionId.java
│   │   │   │   └── TransactionRepository.java
│   │   │   └── infrastructure/          # Adaptadores externos
│   │   │       └── http/
│   │   │           ├── TransactionController.java
│   │   │           ├── request/
│   │   │           │   └── TransactionRequest.java
│   │   │           └── response/
│   │   │               ├── FinancialSummaryResponse.java
│   │   │               └── TransactionResponse.java
│   │   └── resources/
│   │       ├── application.properties   # Configurações
│   │       └── prompts/
│   │           └── system-message.st    # Prompt da IA
│   └── test/
│       └── java/dio/budgeting/
│           └── GetFinancialSummaryUseCaseTest.java
└── README.md                            # Este arquivo
```

---

<div align="center">

Feito com 💚 durante o **Bootcamp Santander Backend Java** da [DIO](https://www.dio.me/)

</div>
