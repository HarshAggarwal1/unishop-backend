package com.unishop.unishop_backend.controller;

import com.unishop.unishop_backend.service.OrderNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders/stream")
public class OrderSseController {

    private final OrderNotificationService notificationService;

    @Autowired
    public OrderSseController(OrderNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public SseEmitter streamOrderUpdates() {
        return notificationService.subscribe();
    }

}
