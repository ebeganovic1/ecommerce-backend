package com.example.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
