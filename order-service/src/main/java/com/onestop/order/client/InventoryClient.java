package com.onestop.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onestop.order.client.dto.ClientDtos.ReservationResult;
import com.onestop.order.client.dto.ClientDtos.ReserveLine;
import com.onestop.order.client.dto.ClientDtos.ReserveRequest;
import com.onestop.order.error.ApiExceptions.DependencyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryClient {

    private final RestClient inventoryRestClient;
    private final ObjectMapper objectMapper;

    public InventoryClient(RestClient inventoryRestClient, ObjectMapper objectMapper) {
        this.inventoryRestClient = inventoryRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Reserve all lines atomically. Throws {@link InventoryInsufficientStockException}
     * (from a 409) listing the product ids that could not be reserved.
     */
    public ReservationResult reserve(Long orderId, List<ReserveLine> items) {
        try {
            return inventoryRestClient.post()
                    .uri("/internal/inventory/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReserveRequest(orderId, items))
                    .retrieve()
                    .body(ReservationResult.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new InventoryInsufficientStockException(parseUnavailable(e.getResponseBodyAsString()));
        } catch (Exception e) {
            throw new DependencyException("Inventory reservation failed: " + e.getMessage());
        }
    }

    public void confirm(Long reservationId) {
        inventoryRestClient.post()
                .uri("/internal/inventory/reservations/{id}/confirm", reservationId)
                .retrieve()
                .toBodilessEntity();
    }

    public void release(Long reservationId) {
        try {
            inventoryRestClient.delete()
                    .uri("/internal/inventory/reservations/{id}", reservationId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Best-effort during recovery.
        }
    }

    public void restock(List<ReserveLine> items) {
        inventoryRestClient.post()
                .uri("/internal/inventory/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReserveRequest(null, items))
                .retrieve()
                .toBodilessEntity();
    }

    private List<Long> parseUnavailable(String body) {
        List<Long> ids = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(body).get("unavailableProductIds");
            if (node != null && node.isArray()) {
                node.forEach(n -> ids.add(n.asLong()));
            }
        } catch (Exception ignored) {
            // Leave the list empty if the body can't be parsed.
        }
        return ids;
    }
}
