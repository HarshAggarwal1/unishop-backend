package com.unishop.unishop_backend.controller;

import com.razorpay.Order;
import com.unishop.unishop_backend.model.OrderStatus;
import com.unishop.unishop_backend.service.OrderService;
import com.unishop.unishop_backend.service.PaymentService;
import com.unishop.unishop_backend.service.PaymentVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
//    private final OrderService orderService;
    private final PaymentVerificationService paymentVerificationService;

    @Value("${razorpay.keyId}")
    private String razorpayKeyId;

    @Autowired
    public PaymentController(
            PaymentService paymentService,
//            OrderService orderService,
            PaymentVerificationService paymentVerificationService)
    {
        this.paymentService = paymentService;
//        this.orderService = orderService;
        this.paymentVerificationService = paymentVerificationService;
    }

    @GetMapping
    public ResponseEntity<?> createOrder(
//            @RequestParam Long orderId,
            @RequestParam(defaultValue = "0.0") String reqAmount,
            @RequestParam(defaultValue = "INR") String currency)
    {

//        com.unishop.unishop_backend.entity.Order order = orderService.getOrderById(orderId).isPresent() ?
//                orderService.getOrderById(orderId).get() : null;
//
//        if (order == null) {
//            return ResponseEntity.badRequest().body("Order not found");
//        }

//        double amount = order.getTotalPrice();
        double amount = Double.parseDouble(reqAmount);

        try {
            Order razorpayOrder = paymentService.createRazorpayOrder(amount, currency);
            if (razorpayOrder != null) {
                String razorpayOrderId = razorpayOrder.get("id");
//                String razorpaySignature = razorpayOrder.get("signature");

//                boolean isVerified = paymentVerificationService.verifyPayment(razorpayOrderId, orderId.toString(), razorpaySignature);

                if (false) {
                    return ResponseEntity.badRequest().body("Payment verification failed");
                }

//                orderService.updateOrderStatus(orderId, OrderStatus.PLACED);
                return ResponseEntity.ok(Map.of("orderId", razorpayOrderId, "amount", amount * 100, "currency", currency, "key", this.razorpayKeyId));
            } else {
                return ResponseEntity.badRequest().body("Failed to create order");
            }
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpaySignature)
    {
        try {
            boolean isVerified = paymentVerificationService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);
            if (isVerified) {
                return ResponseEntity.ok(Map.of("message", "Payment verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Payment verification failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
