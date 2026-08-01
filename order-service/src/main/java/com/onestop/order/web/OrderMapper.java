package com.onestop.order.web;

import com.onestop.order.domain.Order;
import com.onestop.order.domain.OrderAddress;
import com.onestop.order.domain.OrderItem;
import com.onestop.order.web.dto.OrderDtos.OrderAddressDto;
import com.onestop.order.web.dto.OrderDtos.OrderDto;
import com.onestop.order.web.dto.OrderDtos.OrderItemDto;
import com.onestop.order.web.dto.OrderDtos.OrderSummaryDto;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderItemDto toItemDto(OrderItem i) {
        return new OrderItemDto(i.getId(), i.getProductId(), i.getSku(), i.getProductName(),
                i.getQuantity(), i.getUnitPrice(), i.getLineTotal());
    }

    public static OrderAddressDto toAddressDto(OrderAddress a) {
        if (a == null) return null;
        return new OrderAddressDto(a.getRecipientName(), a.getPhone(), a.getLine1(), a.getLine2(),
                a.getCity(), a.getState(), a.getPostalCode(), a.getCountry());
    }

    public static OrderDto toOrderDto(Order o, OrderAddress address) {
        List<OrderItemDto> items = o.getItems().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(OrderMapper::toItemDto)
                .toList();
        return new OrderDto(o.getId(), o.getStatus(), o.getSubtotal(), o.getTax(),
                o.getDeliveryFee(), o.getDiscount(), o.getCouponCode(), o.getTotal(),
                o.getPaymentMethod(), items, toAddressDto(address));
    }

    public static OrderSummaryDto toSummary(Order o) {
        int itemCount = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        return new OrderSummaryDto(o.getId(), o.getStatus(), o.getTotal(), o.getPaymentMethod(), itemCount);
    }
}
