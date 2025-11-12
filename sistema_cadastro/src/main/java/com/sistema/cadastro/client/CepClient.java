package com.sistema.cadastro.client;

import com.sistema.cadastro.dto.CepDataDTO;
import com.sistema.cadastro.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CepClient {

    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(CepClient.class);

    @Value("${app.cep.api.url:https://viacep.com.br/ws}")
    private String cepApiUrl;

    @Value("${app.cep.api.timeout:5000}")
    private int timeout;

    @Value("${app.cep.api.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.cep.api.retry.backoff-delay:1000}")
    private long backoffDelay;

@Cacheable(value = "cepData", key = "#cep", unless = "#result == null")
    public CepDataDTO getCepData(String cep) {
        log.info("Consultando CEP: {}", cep);
        
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{cep}/json/")
                        .build(cep))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.error("Erro 4xx ao consultar CEP: {}", cep);
                    return Mono.error(new BusinessException("CEP invÃ¡lido ou nÃ£o encontrado"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("Erro 5xx ao consultar CEP: {}", cep);
                    return Mono.error(new BusinessException("Erro no serviÃ§o de consulta de CEP"));
                })
                .bodyToMono(CepDataDTO.class)
                .retryWhen(reactor.util.retry.Retry.backoff(maxRetryAttempts, Duration.ofMillis(backoffDelay))
                        .filter(this::isRetryableException)
                        .doAfterRetry(signal -> log.warn("Tentativa {} falhou para CEP: {}", signal.totalRetries() + 1, cep)))
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.info("CEP {} consultado com sucesso", cep))
                .doOnError(error -> log.error("Erro ao consultar CEP {}: {}", cep, error.getMessage()))
                .block();
    }

    private boolean isRetryableException(Throwable throwable) {
        return throwable instanceof WebClientResponseException &&
               ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }

    public CepDataDTO fallbackCepData(String cep, Exception ex) {
        log.error("Fallback ativado para CEP {} devido a: {}", cep, ex.getMessage());

        CepDataDTO dto = new CepDataDTO();
        dto.setCep(cep);
        dto.setErro(true);
        return dto;
    }
}
