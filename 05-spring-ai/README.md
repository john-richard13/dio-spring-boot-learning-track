Desafio de Projeto — Spring AI (Budgeting API)

# Projeto final do módulo 05-spring-ai da trilha dio-spring-boot-learning-track, evoluído com uma melhoria simples: consulta de resumo financeiro.

O que o projeto faz?

É uma API de orçamento pessoal que processa comandos de voz para registrar e consultar transações financeiras. O fluxo principal:

    A pessoa envia um áudio (ex.: "gastei 50 reais no mercado").
    O áudio é transcrito em texto (TranscriptionModel, Whisper).
    Um ChatClient interpreta a intenção e decide qual ferramenta (@Tool) chamar.
    A ferramenta executa um caso de uso real da aplicação (registrar ou consultar transações).
    A resposta final é convertida de volta em áudio (TextToSpeechModel) e devolvida à pessoa.

A arquitetura segue os mesmos princípios de DDD/Clean Architecture usados no restante da trilha: domain (regras e contratos), application (casos de uso, reutilizados tanto pelo REST quanto pela IA) e infrastructure (HTTP, JPA e integração com os modelos de IA).

Melhoria implementada: resumo financeiro

Ideia escolhida da lista: "Adicionar novos tipos de consulta financeira".

Antes, a API só permitia listar transações por categoria. Foi adicionado um novo caso de uso, GetFinancialSummaryUseCase, que calcula:

    o valor total já gasto;
    o total gasto em cada categoria (GROCERIES, PHARMA, AUTO).

Essa consulta foi exposta de duas formas, reaproveitando o mesmo caso de uso (mesmo princípio já usado no projeto para PersistTransactionUseCase e ListTransactionsByCategoryUseCase):

    Endpoint REST: GET /transactions/summary
    Ferramenta de IA (@Tool): registrada no ChatClient, para que a pessoa possa perguntar por voz, por exemplo, "quanto eu já gastei esse mês?" ou "me dá um resumo dos meus gastos".

O prompt do sistema (system-message.st) foi ajustado com uma instrução curta para o modelo saber quando chamar essa nova ferramenta.

# Arquivos alterados/criados

Planilhas
Arquivo	O que mudou
domain/TransactionRepository.java	novo método findAll() no contrato do repositório
infrastructure/persistence/repository/JpaTransactionRepository.java	implementação de findAll() sobre o JPA (abordagem didática com for explícito)
application/output/FinancialSummaryOutput.java	(novo) saída do caso de uso
application/GetFinancialSummaryUseCase.java	(novo) caso de uso + @Tool
infrastructure/http/response/FinancialSummaryResponse.java	(novo) resposta HTTP
infrastructure/http/TransactionController.java	novo endpoint GET /transactions/summary e registro da nova tool no ChatClient
src/main/resources/prompts/system-message.st	instrução para o modelo usar a nova ferramenta
src/test/.../GetFinancialSummaryUseCaseTest.java	(novo) teste unitário do caso de uso
O diff completo está em financial-summary.patch, pronto para aplicar sobre um clone do repositório com git apply financial-summary.patch (rodado a partir da raiz do repo).

Por que essa abordagem

    Não bypassa a arquitetura: o cálculo do resumo vive num caso de uso da camada application, igual aos demais — nada de lógica de negócio no controller.
    Reaproveitamento real: o mesmo caso de uso atende ao endpoint REST e à IA, exatamente como o projeto original já fazia com as outras duas operações.
    Código propositalmente simples: a soma é feita com for e um Map para acumular os totais por categoria, sem streams ou expressões mais avançadas — para ficar fácil de acompanhar o raciocínio linha a linha.

Tecnologias usadas

    Java 25 + Spring Boot
    Spring AI (ChatClient, Tool Calling, TranscriptionModel, TextToSpeechModel)
    OpenAI (gpt-4o-mini, whisper-1, gpt-4o-mini-tts) como provedor
    Spring Data JPA + MySQL (via Docker Compose)
    Lombok
    JUnit 5, Mockito e AssertJ (testes)

Como executar
Pré-requisitos

    Java 25 instalado
    Docker e Docker Compose instalados
    Chave de API da OpenAI (platform.openai.com)

Passo a passo
bash

# 1. Clone o repositório da trilha
git clone https://github.com/digitalinnovationone/dio-spring-boot-learning-track.git
cd dio-spring-boot-learning-track/05-spring-ai

# 2. (Opcional) Aplique o patch com a melhoria, se estiver partindo do projeto base
git apply financial-summary.patch

# 3. Configure a chave da OpenAI
export OPENAI_API_KEY="sua_chave_aqui"
# No Windows (PowerShell): $env:OPENAI_API_KEY="sua_chave_aqui"
# No Windows (CMD): set OPENAI_API_KEY=sua_chave_aqui

# 4. Suba o banco de dados MySQL
docker compose up -d

# 5. Execute a aplicação
./gradlew bootRun

O compose.yml já sobe o banco MySQL automaticamente junto com a aplicação (Docker Compose support do Spring Boot).
Como testar o fluxo principal
1. Registrar uma transação (REST, sem IA)
bash

curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Supermercado","category":"GROCERIES","amount":5000}'

    O campo amount é enviado em centavos (5000 = R$ 50,00).

2. Consultar por categoria
bash

curl http://localhost:8080/transactions/GROCERIES

3. Consultar o resumo financeiro (a melhoria)
bash

curl http://localhost:8080/transactions/summary

Resposta esperada (exemplo com duas transações cadastradas):
JSON

{
  "total": 70.00,
  "totalByCategory": { "GROCERIES": 50.00, "PHARMA": 20.00, "AUTO": 0.00 }
}

4. Fluxo completo por voz (áudio → IA → tool → áudio)
bash

curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@caminho/para/audio.m4a" \
  --output resposta.mp3

Grave um áudio perguntando, por exemplo, "quanto eu já gastei no total?" — o modelo deve identificar a intenção de consulta, chamar a ferramenta get-financial-summary e devolver a resposta falada em resposta.mp3.
5. Testes automatizados
bash

./gradlew test

Planilhas
Teste	Tipo	Precisa de OpenAI?
GetFinancialSummaryUseCaseTest	Unitário (Mockito)	❌ Não
OpenAiChatClientIT	Integração	✅ Sim
ToolCallingIT	Integração	✅ Sim
O novo GetFinancialSummaryUseCaseTest roda sem precisar de chave da OpenAI (é um teste unitário com Mockito). Os testes de integração com a IA continuam exigindo OPENAI_API_KEY configurada.
Para rodar apenas o teste unitário:
bash

./gradlew test --tests "dio.budgeting.application.GetFinancialSummaryUseCaseTest"

O que aprendi

    Como o Tool Calling do Spring AI depende de reaproveitar casos de uso já existentes: a mesma classe anotada com @Tool pode ser injetada tanto num ChatClient quanto usada diretamente por um controller REST, sem duplicar regra de negócio.
    Que o system-message.st funciona como uma "camada de decisão": pequenas instruções nele mudam quando o modelo escolhe usar cada ferramenta, sem tocar em código.
    A importância de manter a lógica de cálculo na camada application e não no controller, para que a mesma regra sirva tanto para voz quanto para REST.
    Como testar um caso de uso isoladamente com Mockito, sem precisar subir o contexto do Spring nem gastar chamadas de API paga durante o desenvolvimento.
    Que código "simples" (como um for explícito em vez de stream) pode ser uma escolha válida quando o objetivo é didático — desde que a intenção seja documentada.