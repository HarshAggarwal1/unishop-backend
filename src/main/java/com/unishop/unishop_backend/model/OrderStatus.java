package com.unishop.unishop_backend.model;

public enum OrderStatus {
    PENDING,
    PLACED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED;

    public static OrderStatus fromValue(int status) {
        return switch (status) {
            case 0 -> PENDING;
            case 1 -> PLACED;
            case 2 -> PROCESSING;
            case 3 -> SHIPPED;
            case 4 -> DELIVERED;
            case 5 -> CANCELLED;
            case 6 -> RETURNED;
            default -> null;
        };
    }
}
