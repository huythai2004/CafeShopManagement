package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.Customer;
import com.fu.cafeshop.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    @Transactional
    public Customer createOrUpdateCustomer(String name, String phone, String email) {
        // Try to find existing customer by phone or email
        Optional<Customer> existing = Optional.empty();
        
        if (phone != null && !phone.isBlank()) {
            existing = customerRepository.findByPhone(phone);
        }
        
        if (existing.isEmpty() && email != null && !email.isBlank()) {
            existing = customerRepository.findByEmail(email);
        }

        if (existing.isPresent()) {
            // Update existing customer
            Customer customer = existing.get();
            if (name != null && !name.isBlank()) {
                customer.setFullName(name);
            }
            if (email != null && !email.isBlank()) {
                customer.setEmail(email);
            }
            return customerRepository.save(customer);
        }

        // Generate unique values to avoid UNIQUE constraint violations on NULL columns
        // Both username and email have UNIQUE constraints in the database
        String timestamp = String.valueOf(System.currentTimeMillis());
        String generatedUsername = "guest_" + timestamp;
        String generatedEmail = (email == null || email.isBlank()) ? "guest_" + timestamp + "@temp.local" : email;
        
        // Create new customer
        Customer customer = Customer.builder()
                .fullName(name)
                .phone(phone)
                .email(generatedEmail)
                .username(generatedUsername)
                .build();
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public long countCustomers() {
        return customerRepository.count();
    }
}
