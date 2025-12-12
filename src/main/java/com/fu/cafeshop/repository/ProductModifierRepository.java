package com.fu.cafeshop.repository;

import com.fu.cafeshop.entity.ProductModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductModifierRepository extends JpaRepository<ProductModifier, Long> {

    List<ProductModifier> findByProductId(Long productId);

    List<ProductModifier> findByProductIdAndIsDefaultTrue(Long productId);

    void deleteByProductId(Long productId);
}

