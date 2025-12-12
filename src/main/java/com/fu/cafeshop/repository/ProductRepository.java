package com.fu.cafeshop.repository;

import com.fu.cafeshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsAvailableTrueOrderByDisplayOrderAsc();

    List<Product> findByCategoryIdAndIsAvailableTrue(Long categoryId);

    List<Product> findByIsFeaturedTrueAndIsAvailableTrue();

    @Query("SELECT p FROM Product p WHERE p.isAvailable = true ORDER BY p.displayOrder ASC")
    List<Product> findAllAvailable();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.modifiers WHERE p.id = :id")
    Product findByIdWithModifiers(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isAvailable = true")
    List<Product> searchByName(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.displayOrder ASC")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    long countByIsAvailableTrue();

    long countByCategoryId(Long categoryId);
}

