package com.unishop.unishop_backend.controller;

import com.razorpay.Order;
import com.unishop.unishop_backend.model.OrderStatus;
import com.unishop.unishop_backend.service.OrderService;
import com.unishop.unishop_backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
//    private final PaymentVerificationService paymentVerificationService;

    @Autowired
    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestParam Long orderId, @RequestParam String currency) {

        com.unishop.unishop_backend.entity.Order order = orderService.getOrderById(orderId).isPresent() ?
                orderService.getOrderById(orderId).get() : null;

        if (order == null) {
            return ResponseEntity.badRequest().body("Order not found");
        }

        double amount = order.getTotalPrice();

        try {
            Order razorpayOrder = paymentService.createRazorpayOrder(amount, currency);
            if (razorpayOrder != null) {
                String razorpayOrderId = razorpayOrder.get("id");
                String razorpaySignature = razorpayOrder.get("signature");

//                boolean isVerified = paymentVerificationService.verifyPayment(razorpayOrderId, orderId.toString(), razorpaySignature);

                if (false) {
                    return ResponseEntity.badRequest().body("Payment verification failed");
                }

                orderService.updateOrderStatus(orderId, OrderStatus.PLACED);
                return ResponseEntity.ok("Order created successfully. Razorpay Order ID: " + razorpayOrderId);
            } else {
                return ResponseEntity.badRequest().body("Failed to create order");
            }
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
