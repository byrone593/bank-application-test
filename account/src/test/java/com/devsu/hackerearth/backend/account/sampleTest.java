package com.devsu.hackerearth.backend.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.devsu.hackerearth.backend.account.controller.AccountController;
import com.devsu.hackerearth.backend.account.model.ClientInfo;
import com.devsu.hackerearth.backend.account.model.dto.AccountDto;
import com.devsu.hackerearth.backend.account.model.dto.TransactionDto;
import com.devsu.hackerearth.backend.account.repository.ClientInfoRepository;
import com.devsu.hackerearth.backend.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class sampleTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ClientInfoRepository clientInfoRepository;
	private AccountService accountService = mock(AccountService.class);
	private AccountController accountController = new AccountController(accountService);

	@Test
	void createAccountTest() {
		// Arrange
		AccountDto newAccount = new AccountDto(1L, "number", "savings", 0.0, true, 1L);
		AccountDto createdAccount = new AccountDto(1L, "number", "savings", 0.0, true, 1L);
		when(accountService.create(newAccount)).thenReturn(createdAccount);

		// Act
		ResponseEntity<AccountDto> response = accountController.create(newAccount);

		// Assert
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(createdAccount, response.getBody());
	}

	@Test
void deposit_withdrawalWithoutFunds_andReport() throws Exception {
    clientInfoRepository.save(new ClientInfo(1L, "Jose Lema", true));

    AccountDto account = new AccountDto(null, "478758", "Ahorro", 100.0, true, 1L);
    String accountResponse = mockMvc.perform(post("/api/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(account)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
    long accountId = objectMapper.readTree(accountResponse).get("id").asLong();

    // F2: depósito +50 -> saldo 150
    mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new TransactionDto(null, null, null, 50.0, 0, accountId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.balance").value(150.0));

    // F3: retiro -500 -> "Saldo no disponible"
    mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new TransactionDto(null, null, null, -500.0, 0, accountId))))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Saldo no disponible"));

    // F4: el movimiento rechazado no queda registrado; solo aparece el depósito
    mockMvc.perform(get("/api/transactions/clients/1/report")
            .param("dateTransactionStart", "2000-01-01")
            .param("dateTransactionEnd", "2100-01-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].client").value("Jose Lema"))
            .andExpect(jsonPath("$[0].accountNumber").value("478758"))
            .andExpect(jsonPath("$[0].balance").value(150.0));
}
}

