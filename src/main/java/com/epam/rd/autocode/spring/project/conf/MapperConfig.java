package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
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

        modelMapper.typeMap(Order.class, OrderDTO.class)
                .addMapping(src -> src.getClient().getEmail(), OrderDTO::setClientEmail)
                .addMapping(src -> src.getEmployee().getEmail(), OrderDTO::setEmployeeEmail);

        return modelMapper;
    }
}
