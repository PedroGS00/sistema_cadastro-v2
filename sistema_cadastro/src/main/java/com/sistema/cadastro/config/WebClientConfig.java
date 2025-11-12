package com.sistema.cadastro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder,
                               @Value("${app.cep.api.url:https://viacep.com.br/ws}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
