package com.onestop.order.security;

/**
 * Authenticated caller derived from the JWT. Carries the raw token so the Order
 * Service can forward it when calling the Cart Service on the user's behalf.
 */
public record AuthenticatedUser(Long userId, String email, String token) {
}
