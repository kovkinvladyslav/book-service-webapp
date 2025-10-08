package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper extends BaseMapper<Order, OrderDTO> {
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;

    @Override
    public Order toEntity(OrderDTO dto) {
        Order order = super.toEntity(dto);
        order.setClient(clientRepository.findByEmail(dto.getClientEmail()).orElseThrow());
        order.setEmployee(employeeRepository.findByEmail(dto.getEmployeeEmail()).orElseThrow());
        return order;
    }
}
