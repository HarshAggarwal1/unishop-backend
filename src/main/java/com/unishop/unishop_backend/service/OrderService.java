package com.unishop.unishop_backend.service;

import com.unishop.unishop_backend.entity.Order;
import com.unishop.unishop_backend.entity.User;
import com.unishop.unishop_backend.model.OrderStatus;
import com.unishop.unishop_backend.repository.OrderRepository;
import com.unishop.unishop_backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository, EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Order placeOrder(Order order, String userName) {

        User user = userRepository.findByUsername(userName).isPresent()
                ? userRepository.findByUsername(userName).get()
                : null;
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.addOrder(order);
        order.setOrderItems(order.getOrderItems());

        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long id) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id",
                Order.class
        );
        query.setParameter("id", id);

        try {
            return Optional.of(query.getSingleResult());
        }
        catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public List<Order> getOrdersByUsername(String username) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.username = :username",
                Order.class
        );
        query.setParameter("username", username);
        return query.getResultList();
    }

    public Order updateOrderStatus(Long id, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null;
    }
}
