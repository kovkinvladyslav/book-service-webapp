package com.epam.rd.autocode.spring.project.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String genre;
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;
    private BigDecimal price;
    @Column(name = "publication_year")
    private LocalDate publicationDate;
    private String author;
    @Column(name = "number_of_pages")
    private Integer pages;
    private String characteristics;
    private String description;
    @Enumerated(EnumType.STRING)
    private Language language;
    @ManyToOne
    @JoinColumn(name = "image_id")   // має збігатись із твоїм SQL
    private Image image;

}
