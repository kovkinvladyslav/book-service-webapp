package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookFilterDTO {
    private String name;
    private String author;
    private String genre;
    private Language language;
    private AgeGroup ageGroup;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDate publicationDate;
    private Integer pages;
    private String characteristics;
    private String description;
}