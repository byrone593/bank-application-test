package com.devsu.hackerearth.backend.account.exceptions;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException() {
        super("Saldo no disponible");
    }

}
