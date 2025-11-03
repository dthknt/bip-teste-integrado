# Frontend do Desafio Fullstack Integrado

O projeto foi construído com Angular 18 e segue as práticas do framework.

## 1. Arquitetura "Zoneless" (Sem Zone.js)

Resolvi adotar a nova tendencia de signals para seguir a nova direção do Angular.
Com essa decisão, removi o Zone.js tornando o bundle final menor e controle de detecção de eventos mais granular e rapida.

A configuração principal (`app.config.ts`) utiliza `provideExperimentalZonelessChangeDetection()` (Angular 18) para habilitar o modo *zoneless*.

## 2. Gestão de Estado com Angular Signals

Para complementar a arquitetura *zoneless*, a gestão de estado é feita inteiramente com **Angular Signals**.

## 3. Arquitetura de Componentes (SOLID)

Segui o Princípio da Responsabilidade Única do SOLID, implementando o padrão "Smart" e "Dumb" components.

* **`beneficio-list.component.ts` (Smart):**
  * Ele é o único que conhece e injeta os serviços (`BeneficioService`, `NotificationService`).
  * Ele gere todo o estado (os *signals* `beneficios`, `showForm`, etc.).
  * Ele contém toda a lógica de negócio (ex: `onSave`, `onDelete`).

* **`beneficio-form.component.ts` (Dumb Component):**
  * É o componente de apresentação
  * Ele recebe dados via `@Input()` (o benefício a ser editado) e emite eventos via `@Output()` (`save` ou `close`).
  * Ele apenas reporta o evento e o *payload* do formulário.

* **Edição e Criação:**
  1. O `beneficio-list` (Smart) usa o seu próprio estado (`beneficioSelecionado()`) para saber se está em modo de edição.
  2. Quando o evento `(save)` vem do formulário, o `beneficio-list` pega o *payload* do evento e o ID do seu *próprio estado* para decidir se deve chamar `createBeneficio` ou `updateBeneficio`.

## 4. Comunicação com Backend

Utilizei configuração com arquivo `proxy.conf.json` para evitar problema com CORS.


## 5. Tech Stack
  * Angular 18
  * Typescript
  * RxJS
