package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repository.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.specification.BookSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final GenericMapper<Book, BookDTO> bookMapper;

    private static final String BOOK_NOT_FOUND = "Book not found with name: ";
    private static final String BOOK_ALREADY_EXISTS = "Book already exists: ";


    @Override
    public List<BookDTO> getAllBooks() {
        return bookMapper.toDtoList(bookRepository.findAll());
    }

    @Override
    public Page<BookDTO> searchBookWithPaginationSortingAndFiltering(
            BookFilterDTO filter,
            Pageable pageable,
            String searchPrompt) {

        Specification<Book> specification = BookSpecification.getSpecification(filter, searchPrompt);
        Page<Book> books = bookRepository.findAll(specification, pageable);

        return books.map(bookMapper::toDto);
    }

    @Override
    public List<String> getBooksGenres() {
        return bookRepository.findDistinctGenres();
    }

    @Override
    public Map<BookDTO, Integer> getBooksFromBookItems(List<BookItemDTO> bookItemDTOList) {
        Map<BookDTO, Integer> books = new HashMap<>();
        for(var item : bookItemDTOList) {
            books.put(getBookByName(item.getBook().getName()), item.getQuantity());
        }
        return books;
    }

    @Override
    public Book getEntityByName(String name) {
        return bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found: " + name));
    }


    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND + name));
        return bookMapper.toDto(book);
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO dto) {
        if (bookRepository.findByName(dto.getName()).isPresent()) {
            throw new AlreadyExistException(BOOK_ALREADY_EXISTS + dto.getName());
        }
        Book book = bookMapper.toEntity(dto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookDTO updateBookByName(String oldName, BookDTO dto) {
        Book existing = bookRepository.findByName(oldName)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND + oldName));

        if (!oldName.equals(dto.getName())) {
            if (bookRepository.findByName(dto.getName()).isPresent()) {
                throw new AlreadyExistException("Cannot rename: Book with name '" + dto.getName() + "' already exists");
            }
        }

        bookMapper.updateEntity(dto, existing);
        return bookMapper.toDto(bookRepository.save(existing));
    }

    @Override
    public void deleteBookByName(String name) {
        Book existing = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND + name));
        bookRepository.delete(existing);
    }
}
