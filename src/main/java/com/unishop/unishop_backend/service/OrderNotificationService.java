package com.unishop.unishop_backend.service;

import com.unishop.unishop_backend.model.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderNotificationService {

    private final List<SseEmitter> emitters;

    public OrderNotificationService() {
        this.emitters = new CopyOnWriteArrayList<>();
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    public void sendOrderUpdate(OrderStatus orderStatus) {
        String message = "Order status updated to: " + orderStatus;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ORDER_UPDATE")
                        .data(message));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
