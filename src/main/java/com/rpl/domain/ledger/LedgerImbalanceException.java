package com.rpl.domain.ledger;

public class LedgerImbalanceException extends RuntimeException {
    public LedgerImbalanceException(String message) {
        super(message);
    }
}
