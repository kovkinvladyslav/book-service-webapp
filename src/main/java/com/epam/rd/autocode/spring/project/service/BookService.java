package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BookService {

    List<BookDTO> getAllBooks();

    BookDTO getBookByName(String name);

    BookDTO updateBookByName(String name, BookDTO book);

    void deleteBookByName(String name);

    BookDTO addBook(BookDTO dto);

    Page<BookDTO> searchBookWithPaginationSortingAndFiltering(BookFilterDTO filter, Pageable pageable,
            String searchPrompt);

    List<String> getBooksGenres();

    Map<BookDTO, Integer> getBooksFromBookItems(List<BookItemDTO> bookItemDTOList);

    Book getEntityByName(String name);

    void restoreBook(String name);
}
