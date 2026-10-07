package com.devsu.hackerearth.backend.client.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsu.hackerearth.backend.client.event.ClientEvent;
import com.devsu.hackerearth.backend.client.event.ClientEventPublisher;
import com.devsu.hackerearth.backend.client.exceptions.BusinessException;
import com.devsu.hackerearth.backend.client.exceptions.ResourceNotFoundException;
import com.devsu.hackerearth.backend.client.model.Client;
import com.devsu.hackerearth.backend.client.model.dto.ClientDto;
import com.devsu.hackerearth.backend.client.model.dto.PartialClientDto;
import com.devsu.hackerearth.backend.client.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

	private final ClientRepository clientRepository;
	private final ClientEventPublisher eventPublisher;

	/*public ClientServiceImpl(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}*/

	@Override
	@Transactional(readOnly = true)
	public List<ClientDto> getAll() {
		// Get all clients
		return clientRepository.findAll()
		.stream()
		.map(this::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public ClientDto getById(Long id) {
		// Get clients by id
		return toDto(find(id));
	}

	@Override
	@Transactional
	public ClientDto create(ClientDto clientDto) {
		if (clientDto.getPassword() == null || clientDto.getPassword().trim().isEmpty()) {
            throw new BusinessException("La contraseña es obligatoria");
        }
        if (clientRepository.existsByDni(clientDto.getDni())) {
            throw new BusinessException("Ya existe un cliente con DNI " + clientDto.getDni());
        }
        Client client = new Client();
        apply(client, clientDto);
        Client saved = clientRepository.save(client);
        publish(saved, saved.isActive());
        return toDto(saved);
	}

	@Override
	@Transactional
	public ClientDto update(ClientDto clientDto) {
		Client client = find(clientDto.getId());
        if (!client.getDni().equals(clientDto.getDni()) && clientRepository.existsByDni(clientDto.getDni())) {
            throw new BusinessException("Ya existe un cliente con DNI " + clientDto.getDni());
        }
        apply(client, clientDto);
        Client saved = clientRepository.save(client);
        publish(saved, saved.isActive());
        return toDto(saved);
	}

	@Override
	@Transactional
    public ClientDto partialUpdate(Long id, PartialClientDto partialClientDto) {
        Client client = find(id);
        client.setActive(partialClientDto.isActive());
        Client saved = clientRepository.save(client);
        publish(saved, saved.isActive());
        return toDto(saved);
    }

	@Override
	@Transactional
	public void deleteById(Long id) {
		Client client = find(id);
        clientRepository.delete(client);
        publish(client, false);
	}


	private Client find(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    private void publish(Client client, boolean active) {
        eventPublisher.publish(new ClientEvent(client.getId(), client.getName(), active));
    }

    private void apply(Client client, ClientDto dto) {
        client.setDni(dto.getDni());
        client.setName(dto.getName());
        client.setGender(dto.getGender());
        client.setAge(dto.getAge());
        client.setAddress(dto.getAddress());
        client.setPhone(dto.getPhone());
        client.setActive(dto.isActive());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            client.setPassword(dto.getPassword()); // en producción: BCrypt
        }
    }

	private ClientDto toDto(Client c){
		// la contraseña nunca se devuelve
		return new ClientDto(c.getId(), c.getDni(), c.getName(), null, c.getGender(), c.getAge(),
	c.getAddress(), c.getPhone(), c.isActive());
	}
}
