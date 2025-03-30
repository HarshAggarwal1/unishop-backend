package com.unishop.unishop_backend.controller;

import com.unishop.unishop_backend.entity.Product;
import com.unishop.unishop_backend.entity.User;
import com.unishop.unishop_backend.model.UserRole;
import com.unishop.unishop_backend.repository.UserRepository;
import com.unishop.unishop_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    @Autowired
    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {

        int userCheck = isUser();
        if (userCheck == -1) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to create products");
        }
        else if (userCheck == -2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to create products");
        }

        Product created = productService.createProduct(product);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {

        int userCheck = isUser();
        if (userCheck == -1) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to create products");
        }
        else if (userCheck == -2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to create products");
        }

        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {

        int userCheck = isUser();
        if (userCheck == -1) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to create products");
        }
        else if (userCheck == -2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to create products");
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private int isUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUsername == null || currentUsername.isEmpty()) {
            return -1;
        }
        UserRole userRole = userRepository.findByUsername(currentUsername).isPresent() ?
                userRepository.findByUsername(currentUsername).get().getRole() : UserRole.ROLE_USER;
        if (userRole == UserRole.ROLE_USER) {
            return -2;
        }
        return 0;
    }

}
