package com.opspilot.exception;

public class ConflictingOrderNumberException extends RuntimeException {

    public ConflictingOrderNumberException(String orderNumber) {
        super("Order number already exists: " + orderNumber);
    }
}
