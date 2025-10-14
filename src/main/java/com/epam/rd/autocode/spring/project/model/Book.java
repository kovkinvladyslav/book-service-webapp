package com.epam.rd.autocode.spring.project.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

@Entity
@Table(name = "books")
@SQLDelete(sql = "UPDATE table_product SET deleted = true WHERE id=?")
@SQLDelete(sql = "UPDATE books SET deleted = true WHERE id = ?")
@FilterDef(name = "deletedBookFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedBookFilter", condition = "deleted = :isDeleted")

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
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = Boolean.FALSE;
}
