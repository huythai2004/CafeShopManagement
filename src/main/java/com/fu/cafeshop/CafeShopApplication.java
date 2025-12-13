package com.fu.cafeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CafeShopApplication {
        public static void main(String[] args) {
            SpringApplication.run(CafeShopApplication.class, args);
        }
}
