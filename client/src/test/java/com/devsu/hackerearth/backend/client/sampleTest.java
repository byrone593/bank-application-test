package com.devsu.hackerearth.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.devsu.hackerearth.backend.client.controller.ClientController;
import com.devsu.hackerearth.backend.client.event.ClientEvent;
import com.devsu.hackerearth.backend.client.event.ClientEventPublisher;
import com.devsu.hackerearth.backend.client.model.Client;
import com.devsu.hackerearth.backend.client.model.Person;
import com.devsu.hackerearth.backend.client.model.dto.ClientDto;
import com.devsu.hackerearth.backend.client.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class sampleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ClientEventPublisher publisher; // evita depender de RabbitMQ en el test
	private ClientService clientService = mock(ClientService.class);
	private ClientController clientController = new ClientController(clientService);

    @Test
    void createClientTest() {
        // Arrange
        ClientDto newClient = new ClientDto(1L, "Dni", "Name", "Password", "Gender", 1, "Address", "9999999999", true);
        ClientDto createdClient = new ClientDto(1L, "Dni", "Name", "Password", "Gender", 1, "Address", "9999999999", true);
        when(clientService.create(newClient)).thenReturn(createdClient);

        // Act
        ResponseEntity<ClientDto> response = clientController.create(newClient);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdClient, response.getBody());
    }

    // F5: prueba unitaria de la entidad de dominio Client (sin Spring)
@Test
void client_keepsPersonAndClientData() {
    Client client = new Client();
    client.setName("Jose Lema");
    client.setDni("1234567890");
    client.setGender("M");
    client.setAge(30);
    client.setAddress("Otavalo sn y principal");
    client.setPhone("098254785");
    client.setPassword("1234");
    client.setActive(true);

    assertTrue(client instanceof Person);
    assertEquals("Jose Lema", client.getName());
    assertEquals("1234567890", client.getDni());
    assertEquals(30, client.getAge());
    assertEquals("1234", client.getPassword());
    assertTrue(client.isActive());
    assertNull(client.getId()); // la PK la asigna la base de datos
}

// F6: prueba de integración (controller -> service -> JPA -> H2)
@Test
void createAndGetClient_endToEnd() throws Exception {
    ClientDto dto = new ClientDto(null, "1717171717", "Marianela Montalvo", "5678", "F", 28,
            "Amazonas y NNUU", "097548965", true);

    String response = mockMvc.perform(post("/api/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.password").doesNotExist()) // la contraseña nunca se devuelve
            .andReturn().getResponse().getContentAsString();

    long id = objectMapper.readTree(response).get("id").asLong();

    mockMvc.perform(get("/api/clients/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Marianela Montalvo"));

    verify(publisher).publish(any(ClientEvent.class)); // se publicó el evento hacia Account
}
}
