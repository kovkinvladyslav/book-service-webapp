package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private final AbstractCrudService<Book, BookDTO, String> crudService;

    private static final String BOOK_NOT_FOUND = "Book not found with name: ";
    private static final String BOOK_ALREADY_EXISTS = "Book already exists: ";

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.crudService = new AbstractCrudService<>(
                bookRepository,
                bookMapper,
                name -> bookRepository.findByName(name)
                        .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND + name)),
                dto -> bookRepository.findByName(dto.getName()).isPresent(),
                dto -> BOOK_ALREADY_EXISTS + dto.getName()
        );
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return crudService.getAll();
    }

    @Override
    public BookDTO getBookByName(String name) {
        return crudService.getByBusinessKey(name);
    }

    @Override
    public BookDTO updateBookByName(String name, BookDTO book) {
        return crudService.update(name, book);
    }

    @Override
    public void deleteBookByName(String name) {
        crudService.delete(name);
    }

    @Override
    public BookDTO addBook(BookDTO book) {
        return crudService.add(book);
    }
}
