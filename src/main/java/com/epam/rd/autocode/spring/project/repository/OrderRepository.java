package com.epam.rd.autocode.spring.project.repository;

import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epam.rd.autocode.spring.project.model.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByClientEmail(String clientEmail);
    List<Order> findByEmployeeEmail(String clientEmail);

    Optional<Order> findByClientEmailAndOrderStatus(String clientEmail, OrderStatus orderStatus);
}
