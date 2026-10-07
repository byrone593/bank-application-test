package com.devsu.hackerearth.backend.client.event;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClientEventPublisher {
    
    private final RestTemplate restTemplate;
    private final String accountUrl;

    public ClientEventPublisher(RestTemplateBuilder builder,
        @Value("${account.service.url:http://localhost:8000}") String accountUrl) {
    this.restTemplate = builder.setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(2)).build();
    this.accountUrl = accountUrl;
}

// Asincrónico: el CRUD de clientes no espera ni depende de que Account responda
@Async
public void publish(ClientEvent event) {
    for (int attempt = 1; attempt <= 3; attempt++) {
        try {
            restTemplate.postForEntity(accountUrl + "/internal/client-events", event, Void.class);
            return;
        } catch (RestClientException ex) {
            log.warn("Intento {}/3: no se pudo notificar el cliente {}: {}", attempt, event.getClientId(),
                    ex.getMessage());
            try {
                Thread.sleep(500L * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    log.error("Evento del cliente {} no entregado tras 3 intentos", event.getClientId());
}

}
