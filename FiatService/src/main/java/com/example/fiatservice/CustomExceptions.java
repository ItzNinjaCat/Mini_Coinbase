package com.example.fiatservice;

public class CustomExceptions {

    public static class UnsupportedCurrencyException extends Exception {
        public UnsupportedCurrencyException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public static class InvalidAmountException extends Exception {
        public InvalidAmountException(String message) {
            super(message);
        }
    }
}
