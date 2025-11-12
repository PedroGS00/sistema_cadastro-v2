# Multi-stage Dockerfile para projeto Java/Spring Boot
# Etapa de build (usa Maven com JDK 17)
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copia somente pom para preparar cache de dependências (pom está dentro de sistema_cadastro)
COPY sistema_cadastro/pom.xml ./pom.xml
RUN mvn -B -q -DskipTests dependency:go-offline

# Copia código-fonte do módulo
COPY sistema_cadastro/src ./src

# Compila o projeto (gera jar em target/)
RUN mvn -B -DskipTests package

# Etapa de runtime (JRE 17 enxuta)
FROM eclipse-temurin:21-jre

ENV APP_HOME=/app
WORKDIR $APP_HOME

# Copia o artefato construído
COPY --from=build /app/target/*.jar app.jar

# Porta padrão de aplicações Spring Boot
EXPOSE 8080

# Variáveis padrão (podem ser sobrescritas via Compose)
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS=""

# Entrada do container
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]