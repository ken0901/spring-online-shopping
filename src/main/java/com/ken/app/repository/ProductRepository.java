package com.ken.app.repository;

import com.ken.app.model.Product;
import com.ken.app.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<ProductWrapper> getAllProduct();
}
