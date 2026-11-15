package com.example.apitester.exception;

/** Thrown when command-line arguments are invalid. */
public class CliException extends RuntimeException {

    public CliException(String message) {
        super(message);
    }
}