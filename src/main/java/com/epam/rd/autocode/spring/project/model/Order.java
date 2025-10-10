package com.epam.rd.autocode.spring.project.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private Client client;
	@ManyToOne
	private Employee employee;
	private LocalDateTime orderDate;
	private BigDecimal price;
	@OneToMany(mappedBy = "order")
	private List<BookItem> bookItems;
}
