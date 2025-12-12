package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.Product;
import com.fu.cafeshop.entity.ProductModifier;
import com.fu.cafeshop.repository.ProductModifierRepository;
import com.fu.cafeshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductModifierRepository modifierRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findAllAvailable();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> getAvailableProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsAvailableTrue(categoryId);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsAvailableTrue();
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByName(keyword);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product getProductWithModifiers(Long id) {
        Product product = productRepository.findByIdWithModifiers(id);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        return product;
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setDefaultPrice(productDetails.getDefaultPrice());
        product.setCostPrice(productDetails.getCostPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setIsAvailable(productDetails.getIsAvailable());
        product.setIsFeatured(productDetails.getIsFeatured());
        product.setDisplayOrder(productDetails.getDisplayOrder());
        return productRepository.save(product);
    }

    @Transactional
    public void toggleAvailability(Long id) {
        Product product = getProductById(id);
        product.setIsAvailable(!product.getIsAvailable());
        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setIsAvailable(false);
        productRepository.save(product);
    }

    // Modifier methods
    public List<ProductModifier> getProductModifiers(Long productId) {
        return modifierRepository.findByProductId(productId);
    }

    @Transactional
    public ProductModifier addModifier(Long productId, ProductModifier modifier) {
        Product product = getProductById(productId);
        modifier.setProduct(product);
        return modifierRepository.save(modifier);
    }

    @Transactional
    public void deleteModifier(Long modifierId) {
        modifierRepository.deleteById(modifierId);
    }

    public long countAvailableProducts() {
        return productRepository.countByIsAvailableTrue();
    }

    public long countProductsByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
}

