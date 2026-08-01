package com.onestop.identity.error;

/** Domain exceptions mapped to HTTP status codes by {@code ApiExceptionHandler}. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** 409 - registration with an email that already exists. */
    public static class EmailAlreadyUsedException extends RuntimeException {
        public EmailAlreadyUsedException(String email) {
            super("Email already registered: " + email);
        }
    }

    /** 401 - bad email/password on login. */
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid email or password");
        }
    }

    /** 404 - entity not found. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
