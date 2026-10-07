package com.devsu.hackerearth.backend.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsu.hackerearth.backend.account.exceptions.BusinessException;
import com.devsu.hackerearth.backend.account.exceptions.ResourceNotFoundException;
import com.devsu.hackerearth.backend.account.model.Account;
import com.devsu.hackerearth.backend.account.model.dto.AccountDto;
import com.devsu.hackerearth.backend.account.model.dto.PartialAccountDto;
import com.devsu.hackerearth.backend.account.repository.AccountRepository;
import com.devsu.hackerearth.backend.account.repository.ClientInfoRepository;
import com.devsu.hackerearth.backend.account.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;
    private final ClientInfoRepository clientInfoRepository;
    private final TransactionRepository transactionRepository;

	@Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAll() {
        // Get all accounts
        return accountRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getById(Long id) {
        // Get accounts by id
		return toDto(find(id));
    }

    @Override
    @Transactional
    public AccountDto create(AccountDto accountDto) {
        // Create account
		// Consistencia eventual: si el cliente aún no llegó por el evento se acepta; si llegó inactivo, se rechaza
        clientInfoRepository.findById(accountDto.getClientId()).ifPresent(c -> {
            if (!c.isActive()) {
                throw new BusinessException("El cliente está inactivo");
            }
        });
        if (accountRepository.existsByNumber(accountDto.getNumber())) {
            throw new BusinessException("Ya existe una cuenta con número " + accountDto.getNumber());
        }
        Account account = new Account();
        account.setNumber(accountDto.getNumber());
        account.setType(accountDto.getType());
        account.setInitialAmount(accountDto.getInitialAmount());
        account.setActive(accountDto.isActive());
        account.setClientId(accountDto.getClientId());
        return toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto update(AccountDto accountDto) {
        // Update account
		Account account = find(accountDto.getId());
        if (!account.getNumber().equals(accountDto.getNumber()) && accountRepository.existsByNumber(accountDto.getNumber())) {
            throw new BusinessException("Ya existe una cuenta con número " + accountDto.getNumber());
        }
        // initialAmount y clientId no cambian: el saldo solo se mueve con transacciones
        account.setNumber(accountDto.getNumber());
        account.setType(accountDto.getType());
        account.setActive(accountDto.isActive());
        return toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto partialUpdate(Long id, PartialAccountDto partialAccountDto) {
        // Partial update account
		Account account = find(id);
        account.setActive(partialAccountDto.isActive());
        return toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // Delete account
        Account account = find(id);
        if (transactionRepository.existsByAccountId(id)) {
            throw new BusinessException("No se puede eliminar una cuenta con movimientos; desactívela");
        }
        accountRepository.delete(account);
    }
    
    private Account find(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + id));
    }

    private AccountDto toDto(Account a) {
        return new AccountDto(a.getId(), a.getNumber(), a.getType(), a.getInitialAmount(), a.isActive(),
                a.getClientId());
    }
}
