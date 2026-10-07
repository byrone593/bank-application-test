package com.devsu.hackerearth.backend.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsu.hackerearth.backend.account.event.ClientEvent;
import com.devsu.hackerearth.backend.account.model.ClientInfo;
import com.devsu.hackerearth.backend.account.repository.ClientInfoRepository;

import lombok.RequiredArgsConstructor;



// Endpoint interno: recibe los eventos de Client y mantiene la réplica local de clientes
@RestController
@RequestMapping("/internal/client-events")
@RequiredArgsConstructor
public class ClientEventController {

    private final ClientInfoRepository clientInfoRepository;

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody ClientEvent event) {
        clientInfoRepository.save(new ClientInfo(event.getClientId(), event.getName(), event.isActive()));
        return ResponseEntity.accepted().build();
    }
}
