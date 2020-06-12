package com.financial.solutions.incomesynchronization.exception;

public class BusinessException extends Exception{
    private static final String DEFAULT_MESSAGE = "It was not possible to process the synchronization because ";
    public BusinessException (String exceptionMessage) {
        super(DEFAULT_MESSAGE+exceptionMessage);
    }
}
