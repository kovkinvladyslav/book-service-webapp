package com.epam.rd.autocode.spring.project.service.impl;

import java.util.List;

import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.stereotype.Service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
    final private OrderRepository orderRepository;
    final private GenericMapper<Order, OrderDTO> orderMapper;
    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        return orderMapper.toDtoList(orderRepository.findByClientEmail(clientEmail));
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return orderMapper.toDtoList(orderRepository.findByEmployeeEmail(employeeEmail));
    }

    @Override
    public OrderDTO addOrder(OrderDTO orderDTO) {
        Order order = orderMapper.toEntity(orderDTO);
        return orderMapper.toDto(orderRepository.save(order));
    }
}
