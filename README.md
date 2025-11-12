# Sistema de Cadastro

Gerenciamento simples de usuários em Java (Maven), com separação por camadas e suporte a build, testes e execução local ou via Docker/Compose.

Nota: este repositório substitui o anterior hospedado em `https://github.com/PedroGS00/sistema-de-cadastro`.

---

## 1. Título e Descrição

- Nome: Sistema de Cadastro
- Descrição:
API RESTful para gerenciamento de usuários e produtos, desenvolvida com foco em práticas modernas de desenvolvimento e testes.

O projeto utiliza **Java 21**, **Spring Boot 3** e **PostgreSQL** para fornecer operações CRUD completas, garantindo robustez e manutenibilidade.
---

## 2. Pré-requisitos

- Dependências
  - Java JDK 17 (LTS) ou superior
  - Apache Maven 3.9.x ou superior
  - Git 2.44.x ou superior
  - Docker Desktop 4.33.x ou superior (opcional, para execução containerizada)
- Sistema operacional
  - Windows 10/11 (suportado oficialmente)
  - Linux/macOS (suportado, comandos podem variar)
- Configurações ambientais obrigatórias (exemplos, ajuste conforme seu ambiente)
  - `DB_URL` (ex.: URL JDBC de banco de dados)
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `SERVER_PORT` (porta HTTP da aplicação)
  - Perfis de execução (se aplicável): `APP_ENV` com valores `dev`, `test` ou `prod`

Arquivos de ambiente recomendados:
- `.env.dev` para desenvolvimento
- `.env.test` para testes
- `.env.prod` para produção

---

## 3. Setup e Instalação

1) Clonar o repositório

```bash
git clone https://github.com/SEU_USUARIO/NOME_DO_REPOSITORIO.git
```

2) Entrar no diretório do projeto

```bash
cd "c:\Users\Usuario\Documentos\Lições\ADO'S\Quarto Semestre\Teste de Software\sistema_cadastro"
```

3) Verificar instalação do Maven

```bash
mvn -v
```

4) Configurar variáveis de ambiente (Windows, sessão atual)

```bash
set DB_URL=jdbc:postgresql://localhost:5432/sistema_cadastro
```

```bash
set DB_USERNAME=usuario
```

```bash
set DB_PASSWORD=senha
```

```bash
set SERVER_PORT=8080
```

```bash
set APP_ENV=dev
```

Para definir permanentemente (Windows):

```bash
setx DB_URL "jdbc:postgresql://localhost:5432/sistema_cadastro"
```

```bash
setx DB_USERNAME "usuario"
```

```bash
setx DB_PASSWORD "senha"
```

```bash
setx SERVER_PORT "8080"
```

```bash
setx APP_ENV "dev"
```

5) Instalar/Resolver dependências Maven (a partir do diretório `sistema_cadastro`)

```bash
cd sistema_cadastro
```

```bash
mvn dependency:resolve
```

Opcional (build rápido sem testes na primeira execução):

```bash
mvn clean install -DskipTests
```

---

## 4. Build e Execução

Build completo (gera artefatos em `sistema_cadastro\target`):

```bash
cd sistema_cadastro
```

```bash
mvn clean install
```

Execução em desenvolvimento (opções comuns — use a que se aplica ao seu projeto):
- Se o projeto for Spring Boot:

```bash
mvn spring-boot:run
```

- Se o projeto gerar um JAR executável:

```bash
java -jar target\sistema_cadastro-<versao>.jar
```

Observação: substitua `<versao>` pela versão definida no `pom.xml`. Liste o conteúdo da pasta `target` para identificar o nome exato do JAR:

```bash
dir target
```

Execução com Docker (a partir da raiz do repositório):

```bash
docker build -t sistema-cadastro:latest .
```

```bash
docker run --rm -p 8080:8080 --env-file .env.dev sistema-cadastro:latest
```

Execução com Docker Compose (usa `docker-compose.yml`):

```bash
docker-compose --env-file .env.dev up -d
```

Ambientes:
- Desenvolvimento: `.env.dev`, logs em foreground, hot-reload se configurado
- Testes: `.env.test`, base de dados/serviços isolados
- Produção: `.env.prod`, Docker Compose com configurações endurecidas

Flags e parâmetros opcionais úteis:
- Maven: `-DskipTests` (pula testes), `-X` (log do Maven detalhado)
- Perfis (se aplicável): `-Pdev`, `-Pprod`
- Spring Boot (se aplicável): `-Dspring.profiles.active=dev`

```bash
mvn clean install -DskipTests
```

```bash
mvn -X clean install
```

```bash
mvn clean install -Pprod
```

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

---

## 5. Testes

Testes unitários:

```bash
cd sistema_cadastro
```

```bash
mvn test
```

Testes de integração (se configurados com Maven Failsafe):

```bash
mvn verify
```

Executar uma suíte/uma classe específica:

```bash
mvn -Dtest=NomeDaClasseDeTeste test
```

Cobertura de testes (JaCoCo):
- Após `mvn test`, os relatórios costumam estar em `sistema_cadastro\target\site\jacoco\index.html`
- Artefato de execução: `sistema_cadastro\target\jacoco.exec`

Abrir o relatório no Windows:

```bash
start sistema_cadastro\target\site\jacoco\index.html
```

Meta de cobertura esperada:
- Linhas: ≥ 80%
- Ramos: ≥ 70%
(ajuste conforme política do time)

---

## 6. Deploy

Deploy com Docker Compose (produção):

```bash
docker-compose --env-file .env.prod up -d --build
```

Atualização de imagem e reinício dos serviços:

```bash
docker-compose --env-file .env.prod pull
```

```bash
docker-compose --env-file .env.prod up -d
```

Variáveis de ambiente sensíveis devem estar apenas em `.env.prod` (não versionado, quando aplicável). Ajuste volumes, limites de memória/CPU e política de reinício no `docker-compose.yml` conforme sua infraestrutura.

---
