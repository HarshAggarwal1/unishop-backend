package com.unishop.unishop_backend.controller;

import com.unishop.unishop_backend.entity.Order;
import com.unishop.unishop_backend.model.OrderStatus;
import com.unishop.unishop_backend.model.UserRole;
import com.unishop.unishop_backend.repository.UserRepository;
import com.unishop.unishop_backend.service.OrderNotificationService;
import com.unishop.unishop_backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderNotificationService orderNotificationService;

    @Autowired
    public OrderController(
            OrderService orderService,
           UserRepository userRepository,
           OrderNotificationService orderNotificationService)
    {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.orderNotificationService = orderNotificationService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Order order, Principal principal) {
        String currentUserName = principal.getName();
        if (currentUserName == null || currentUserName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        Order placedOrder = orderService.placeOrder(order, currentUserName);
        return ResponseEntity.ok(placedOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String username) {
        List<Order> orders = orderService.getOrdersByUsername(username);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/{status}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @PathVariable int status, Principal principal) {
        String currentUserName = principal.getName();
        int userCheck = isAdmin(currentUserName);
        if (userCheck == -1) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to create products");
        }
        else if (userCheck == -2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to create products");
        }

        OrderStatus orderStatus = OrderStatus.fromValue(status);
        if (orderStatus == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid order status");
        }
        Order updatedOrder = orderService.updateOrderStatus(id, orderStatus);
        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }

        orderNotificationService.sendOrderUpdate(orderStatus);

        return ResponseEntity.ok(updatedOrder);
    }

    private int isAdmin(String currentUserName) {
        if (currentUserName == null || currentUserName.isEmpty()) {
            return -1;
        }
        UserRole userRole = userRepository.findByUsername(currentUserName).isPresent() ?
                userRepository.findByUsername(currentUserName).get().getRole() : UserRole.ROLE_USER;
        if (userRole != UserRole.ROLE_ADMIN) {
            return -2;
        }
        return 0;
    }
}
