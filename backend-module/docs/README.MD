# Solução: Desafio Fullstack Integrado

Este documento descreve a arquitetura e as decisões técnicas implementadas para a solução do Desafio Fullstack.

## 1. Arquitetura da Solução

Para atender aos requisitos de uma aplicação em camadas e seguir os princípios SOLID, a arquitetura foi desenhada com uma separação clara de responsabilidades:

* **`ear-module` (Empacotamento):**
    * A aplicação é empacotada como um **EAR**
    * Esta decisão foi feita para garantir que o `backend-module.war` e o `ejb-module.jar` sejam implantados como uma única aplicação, permitindo a integração entre os contextos Spring e EJB, pelo mesmo classloader.
    
* **`backend-module` (Camada Web - Spring Boot):**
    * Funciona exclusivamente como uma camada **Facade**, expondo a API REST.
    * Suas responsabilidades são: receber requisições HTTP, validar DTOs (Data Transfer Objects) e delegar 100% da lógica de negócio.
    * **Esta camada não tem acesso direto ao banco de dados** (não possui `Repositories` nem `EntityManager`). O JPA do Spring Boot foi desabilitado para garantir essa separação.

* **`ejb-module` (Camada de Negócio e Dados - EJB):**
    * Este é o "cérebro" da aplicação e a única camada que acessa o banco de dados.
    * O `BeneficioEjbService` (um `@Stateless` EJB) centraliza toda a lógica de persistência (CRUD) e as regras de negócio (transferência).

### Integração Spring (Backend) <-> EJB (Negócio)

A comunicação entre a camada Web (Spring) e a camada de Negócio (EJB) é feita via **JNDI Lookup**.

1.  O `ejb-module` registra seus serviços (EJBs) no contexto JNDI do container (TomEE).
2.  O `backend-module` utiliza uma classe de configuração Spring (`EjbJndiConfig`) para "procurar" (lookup) o EJB no JNDI e injetá-lo como um Bean no contexto do Spring.
3.  Isso permite que o `BeneficioServiceImpl` (Spring) injete o `BeneficioEjbServiceLocal` (EJB), mantendo o baixo acoplamento.

---

## 2. Correção do Bug de Transferência

O bug no `BeneficioEjbService` não validava saldos (permitindo valores negativos) e não possuía mecanismo de controle de concorrência (vulnerável a *race conditions* e *lost updates*).

A solução foi feita em três partes:

### A. Princípio da Responsabilidade Única (SOLID)

A lógica de validação de regras de negócio (verificar se contas existem, se o saldo é suficiente) foi separada em outro EJB, `TransferenciaValidator`.

### B. Controle de Concorrência (Lock Pessimista)

Para resolver a *race condition*, a operação de transferência agora utiliza **Lock Pessimista (`PESSIMISTIC_WRITE`)**.

Ao recuperar os beneficios de origem e destino, o `EntityManager` instrui o banco de dados a bloquear essas linhas (`SELECT ... FOR UPDATE`). Qualquer outra transação que tente ler ou alterar essas mesmas linhas, ficará em espera até que a nossa transação seja concluída (com `commit` ou `rollback`).

Isso garante que a validação de saldo e a subtração/adição sejam uma operação atômica, segura e consistente.

### C. Atomicidade (ACID)

Se qualquer regra no `TransferenciaValidator` falhar, ele lança uma `TransferenciaException` (uma `ApplicationException` com `rollback=true`). Isso garante que a transação EJB seja completamente revertida, mantendo a integridade dos dados.

---

## 3. Como Executar (Deploy no TomEE)

1.  **Banco de Dados:**
    * Configure o `DataSource` no arquivo `conf/tomee.xml` (ou `conf/resources.xml`) do seu TomEE.
    * O nome JNDI esperado pelo `persistence.xml` é `jdbc/MeuDataSource`.

2.  **Build:**
    * Execute `mvn clean package` na raiz do projeto.
    * Isso irá gerar o artefato final em `ear-module/target/ear-module-1.0.0.ear`.

3.  **Deploy:**
    * Copie o arquivo `ear-module-1.0.0.ear` para a pasta `webapps/` do TomEE.
    * Inicie o servidor. A aplicação estará disponível (ex: `http://localhost:8080/bip-backend/api/v1/beneficios`).

## 4. Tech Stack
* Java 17
* Spring Boot 3
* Jakarta EE (EJB 4.0)
* Jakarta Persistence (JPA 3.1)
* EclipseLink

