package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    private static final String BOOK_NOT_FOUND = "Book not found with name: ";
    private static final String BOOK_ALREADY_EXISTS = "Book already exists: ";

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookMapper.toDtoList(bookRepository.findAll());
    }

    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND + name));
        return bookMapper.toDto(book);
    }

    @Override
    public BookDTO addBook(BookDTO dto) {
        if (bookRepository.findByName(dto.getName()).isPresent()) {
            throw new AlreadyExistException(BOOK_ALREADY_EXISTS + dto.getName());
        }
        Book book = bookMapper.toEntity(dto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public BookDTO updateBookByName(String name, BookDTO dto) {
        Book existing = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND + name));

        bookMapper.updateEntity(dto, existing);
        return bookMapper.toDto(bookRepository.save(existing));
    }

    @Override
    public void deleteBookByName(String name) {
        Book existing = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND + name));
        bookRepository.delete(existing);
    }

    @Override
    public String getImageUrlByName(String bookName) {
        return bookRepository.findByName(bookName)
                .map(b -> b.getImage().getUrl())
                .orElseThrow(() -> new NotFoundException("image not found for " + bookName));
    }
}
