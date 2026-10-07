package com.devsu.hackerearth.backend.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsu.hackerearth.backend.account.exceptions.BusinessException;
import com.devsu.hackerearth.backend.account.exceptions.InsufficientBalanceException;
import com.devsu.hackerearth.backend.account.exceptions.ResourceNotFoundException;
import com.devsu.hackerearth.backend.account.model.Account;
import com.devsu.hackerearth.backend.account.model.ClientInfo;
import com.devsu.hackerearth.backend.account.model.Transaction;
import com.devsu.hackerearth.backend.account.model.dto.BankStatementDto;
import com.devsu.hackerearth.backend.account.model.dto.TransactionDto;
import com.devsu.hackerearth.backend.account.repository.AccountRepository;
import com.devsu.hackerearth.backend.account.repository.ClientInfoRepository;
import com.devsu.hackerearth.backend.account.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

	private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ClientInfoRepository clientInfoRepository;


    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getAll() {
        // Get all transactions
        return transactionRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getById(Long id) {
        // Get transactions by id
		return toDto(transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado: " + id)));
    }

    @Override
    @Transactional
    public TransactionDto create(TransactionDto transactionDto) {
        // Create transaction
		if (transactionDto.getAccountId() == null) {
            throw new BusinessException("accountId es obligatorio");
        }
        if (!Double.isFinite(transactionDto.getAmount()) || transactionDto.getAmount() == 0) {
            throw new BusinessException("El monto debe ser distinto de cero");
        }
        Account account = accountRepository.findByIdForUpdate(transactionDto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + transactionDto.getAccountId()));
        if (!account.isActive()) {
            throw new BusinessException("La cuenta está inactiva");
        }
        double newBalance = add(currentBalance(account), transactionDto.getAmount());
        if (newBalance < 0) {
            throw new InsufficientBalanceException(); // "Saldo no disponible"
        }
        Transaction tx = new Transaction();
        tx.setAccountId(account.getId());
        tx.setDate(new Date());
        tx.setAmount(transactionDto.getAmount());
        tx.setType(transactionDto.getAmount() > 0 ? "DEPOSITO" : "RETIRO");
        tx.setBalance(newBalance);
        return toDto(transactionRepository.save(tx));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankStatementDto> getAllByAccountClientIdAndDateBetween(Long clientId, Date dateTransactionStart,
            Date dateTransactionEnd) {
        // Report
		if (dateTransactionStart.after(dateTransactionEnd)) {
            throw new BusinessException("dateTransactionStart no puede ser posterior a dateTransactionEnd");
        }
        Map<Long, Account> accounts = accountRepository.findByClientId(clientId).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));
        if (accounts.isEmpty()) {
            return List.of();
        }
        String clientName = clientInfoRepository.findById(clientId).map(ClientInfo::getName).orElse(null);

        return transactionRepository
                .findByAccountIdInAndDateBetweenOrderByDateAscIdAsc(accounts.keySet(), dateTransactionStart,
                        endOfDay(dateTransactionEnd))
                .stream().map(t -> {
                    Account a = accounts.get(t.getAccountId());
                    return new BankStatementDto(t.getDate(), clientName, a.getNumber(), a.getType(),
                            a.getInitialAmount(), a.isActive(), t.getType(), t.getAmount(), t.getBalance());
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getLastByAccountId(Long accountId) {
        // If you need it
		return transactionRepository.findFirstByAccountIdOrderByDateDescIdDesc(accountId).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta " + accountId + " no tiene movimientos"));
    }

    private double currentBalance(Account account) {
        return transactionRepository.findFirstByAccountIdOrderByDateDescIdDesc(account.getId())
                .map(Transaction::getBalance).orElse(account.getInitialAmount());
    }

    // aritmética decimal exacta aunque los campos del contrato sean double
    private static double add(double a, double b) {
        return BigDecimal.valueOf(a).add(BigDecimal.valueOf(b)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // la fecha final del reporte es inclusiva: se extiende hasta el fin de ese día
    private static Date endOfDay(Date date) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate day = date.toInstant().atZone(zone).toLocalDate();
        return Date.from(day.atTime(LocalTime.MAX).atZone(zone).toInstant());
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(t.getId(), t.getDate(), t.getType(), t.getAmount(), t.getBalance(),
                t.getAccountId());
    }
    
}
