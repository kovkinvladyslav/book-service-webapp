package com.epam.rd.autocode.spring.project.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MapperConfig.class)
public class BaseConfig{
    // Place your code here
}
