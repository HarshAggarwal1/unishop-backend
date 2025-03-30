package com.unishop.unishop_backend.repository;

import com.unishop.unishop_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search by category
    List<Product> findByCategory(String category);

    // Search by brand
    List<Product> findByBrand(String brand);

}
