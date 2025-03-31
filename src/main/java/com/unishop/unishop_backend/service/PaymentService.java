package com.unishop.unishop_backend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;

    @Autowired
    public PaymentService(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    public Order createRazorpayOrder(double amount, String currency) {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int)(amount * 100));
        orderRequest.put("currency", currency);
        orderRequest.put("payment_capture", 1);

        Order order;
        try {
            order = razorpayClient.orders.create(orderRequest);
        } catch (Exception e) {
            return null;
        }
        return order;
    }
}
