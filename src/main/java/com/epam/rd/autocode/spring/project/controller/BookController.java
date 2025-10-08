package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    List<BookDTO> getAllBooks() {
        return bookService.getAllBooks();
    }

    @PutMapping("/{name}")
    ResponseEntity<BookDTO> updateBook(
            @PathVariable String name,
            @RequestBody BookDTO bookDTO
    ) {
        BookDTO updatedBook = bookService.updateBookByName(name, bookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @PostMapping
    ResponseEntity<String> addNewBook(@RequestBody BookDTO bookDTO) {
        bookService.addBook(bookDTO);
        return ResponseEntity.ok("Book has been added");
    }
}
