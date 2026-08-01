package com.onestop.wishlist.error;

/** Domain exceptions mapped to HTTP status codes by {@code ApiExceptionHandler}. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** 404 - product does not exist in the catalog. */
    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(Long productId) {
            super("Product not found: " + productId);
        }
    }
}
