package com.mybillbook.exception;

public class OpenAIServiceException extends RuntimeException {

    public OpenAIServiceException(String message) {
        super(message);
    }

    public OpenAIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
