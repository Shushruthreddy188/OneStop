package com.onestop.order.error;

import java.util.List;

/** Domain exceptions mapped to HTTP status codes by {@code ApiExceptionHandler}. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** 400 - checkout attempted with an empty cart. */
    public static class EmptyCartException extends RuntimeException {
        public EmptyCartException() {
            super("Cannot place an order with an empty cart");
        }
    }

    /** 409 - one or more items could not be reserved; no order was confirmed. */
    public static class InsufficientStockException extends RuntimeException {
        private final transient List<String> unavailableSkus;

        public InsufficientStockException(List<String> unavailableSkus) {
            super("Insufficient stock for: " + unavailableSkus);
            this.unavailableSkus = unavailableSkus;
        }

        public List<String> getUnavailableSkus() {
            return unavailableSkus;
        }
    }

    /** 404 - order not found for this customer. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    /** 409 - order is in a state that doesn't allow the operation. */
    public static class OrderStateException extends RuntimeException {
        public OrderStateException(String message) {
            super(message);
        }
    }

    /** 502 - a downstream dependency (cart/catalog/inventory) failed. */
    public static class DependencyException extends RuntimeException {
        public DependencyException(String message) {
            super(message);
        }
    }
}
