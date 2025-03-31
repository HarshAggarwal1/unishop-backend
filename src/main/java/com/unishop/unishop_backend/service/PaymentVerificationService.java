package com.unishop.unishop_backend.service;

import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PaymentVerificationService {

    @Value("${razorpay.keySecret}")
    private String keySecret;

    public boolean verifyPayment(String orderId, String paymentId, String razorpaySignature) {
        try {
            String payload = orderId + "|" + paymentId;
            return Utils.verifySignature(payload, razorpaySignature, keySecret);
        }
        catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Failed to verify payment signature", e);
            return false;
        }
    }
}