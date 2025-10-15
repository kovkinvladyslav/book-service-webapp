package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookDTO {

    @NotBlank(message = "Book name is required")
    @Size(min = 1, max = 200, message = "Book name must be between 1 and 200 characters")
    private String name;

    @NotBlank(message = "Genre is required")
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;

    @NotNull(message = "Age group is required")
    private AgeGroup ageGroup;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "9999.99", message = "Price must not exceed $9999.99")
    @Digits(integer = 4, fraction = 2, message = "Price must have at most 4 digits and 2 decimal places")
    private BigDecimal price;

    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;

    @NotBlank(message = "Author name is required")
    @Size(min = 2, max = 150, message = "Author name must be between 2 and 150 characters")
    private String author;

    @Min(value = 1, message = "Pages must be at least 1")
    @Max(value = 10000, message = "Pages cannot exceed 10000")
    private Integer pages;

    @Size(max = 500, message = "Characteristics must not exceed 500 characters")
    private String characteristics;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Language is required")
    private Language language;

    private boolean deleted;
}