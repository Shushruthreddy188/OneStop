package com.onestop.inventory.error;

import java.util.List;

/** Domain exceptions mapped to HTTP status codes by {@code ApiExceptionHandler}. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** 409 - one or more products could not be reserved; nothing was reserved. */
    public static class InsufficientStockException extends RuntimeException {
        private final transient List<Long> unavailableProductIds;

        public InsufficientStockException(List<Long> unavailableProductIds) {
            super("Insufficient stock for products: " + unavailableProductIds);
            this.unavailableProductIds = unavailableProductIds;
        }

        public List<Long> getUnavailableProductIds() {
            return unavailableProductIds;
        }
    }

    /** 404 - inventory or reservation not found. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    /** 409 - reservation is in a state that doesn't allow the operation. */
    public static class ReservationStateException extends RuntimeException {
        public ReservationStateException(String message) {
            super(message);
        }
    }
}
