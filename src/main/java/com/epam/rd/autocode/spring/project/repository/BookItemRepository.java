package com.epam.rd.autocode.spring.project.repository;

import com.epam.rd.autocode.spring.project.model.BookItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookItemRepository extends JpaRepository<BookItem, Long> {
}
