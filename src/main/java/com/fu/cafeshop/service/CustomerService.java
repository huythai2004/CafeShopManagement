package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.Customer;
import com.fu.cafeshop.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.io.FileWriter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    // #region agent log
    private static final String DEBUG_LOG_PATH = "d:\\GiaoTrinhHoc\\Ky5\\CafeShopManagement\\CafeShopManagement\\cafeshop\\.cursor\\debug.log";
    private void debugLog(String hypothesisId, String location, String message, String data) {
        try (FileWriter fw = new FileWriter(DEBUG_LOG_PATH, true)) {
            fw.write(String.format("{\"hypothesisId\":\"%s\",\"location\":\"%s\",\"message\":\"%s\",\"data\":%s,\"timestamp\":%d,\"sessionId\":\"debug-session\"}\n",
                hypothesisId, location, message, data, System.currentTimeMillis()));
        } catch (IOException e) { /* ignore */ }
    }
    // #endregion

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
        // #region agent log
        debugLog("A,B,C", "CustomerService.java:createOrUpdateCustomer:entry", "Method entry with params", 
            String.format("{\"name\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\"}", name, phone, email));
        // #endregion
        
        // Try to find existing customer by phone or email
        Optional<Customer> existing = Optional.empty();
        
        if (phone != null && !phone.isBlank()) {
            existing = customerRepository.findByPhone(phone);
            // #region agent log
            debugLog("C,E", "CustomerService.java:findByPhone", "Phone lookup result", 
                String.format("{\"phone\":\"%s\",\"found\":%b}", phone, existing.isPresent()));
            // #endregion
        }
        
        if (existing.isEmpty() && email != null && !email.isBlank()) {
            existing = customerRepository.findByEmail(email);
            // #region agent log
            debugLog("C", "CustomerService.java:findByEmail", "Email lookup result", 
                String.format("{\"email\":\"%s\",\"found\":%b}", email, existing.isPresent()));
            // #endregion
        }

        if (existing.isPresent()) {
            // Update existing customer
            Customer customer = existing.get();
            // #region agent log
            debugLog("C", "CustomerService.java:updateExisting", "Updating existing customer", 
                String.format("{\"customerId\":%d,\"existingUsername\":\"%s\",\"existingEmail\":\"%s\"}", 
                    customer.getId(), customer.getUsername(), customer.getEmail()));
            // #endregion
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
        
        // #region agent log
        debugLog("A,B", "CustomerService.java:createNew", "Creating new customer with generated values", 
            String.format("{\"name\":\"%s\",\"phone\":\"%s\",\"originalEmail\":\"%s\",\"generatedEmail\":\"%s\",\"username\":\"%s\"}", 
                name, phone, email, generatedEmail, generatedUsername));
        // #endregion
        
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
