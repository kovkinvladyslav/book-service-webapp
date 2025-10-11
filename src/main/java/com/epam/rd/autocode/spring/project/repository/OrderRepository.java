package com.epam.rd.autocode.spring.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epam.rd.autocode.spring.project.model.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByClientEmail(String clientEmail);
    List<Order> findByEmployeeEmail(String clientEmail);
}
