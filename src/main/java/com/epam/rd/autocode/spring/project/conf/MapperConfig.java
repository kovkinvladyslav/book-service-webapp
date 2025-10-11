package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true);
        return modelMapper;
    }

    @Bean("bookMapper")
    public GenericMapper<Book, BookDTO> bookMapper(ModelMapper modelMapper){
        return new GenericMapper<>(modelMapper, Book.class, BookDTO.class);
    }

    @Bean("orderMapper")
    public GenericMapper<Order, OrderDTO> orderMapper(ModelMapper modelMapper) {
        modelMapper.typeMap(Order.class, OrderDTO.class)
                .addMapping(src -> src.getClient().getEmail(), OrderDTO::setClientEmail)
                .addMapping(src -> src.getEmployee().getEmail(), OrderDTO::setEmployeeEmail);

        return new GenericMapper<>(modelMapper, Order.class, OrderDTO.class);
    }

    @Bean("clientMapper")
    public GenericMapper<Client, ClientDTO> clientMapper(ModelMapper modelMapper) {
        return new GenericMapper<>(modelMapper, Client.class, ClientDTO.class);
    }

    @Bean("employeeMapper")
    public GenericMapper<Employee, EmployeeDTO> employeeMapper(ModelMapper modelMapper) {
        return new GenericMapper<>(modelMapper, Employee.class, EmployeeDTO.class);
    }
}