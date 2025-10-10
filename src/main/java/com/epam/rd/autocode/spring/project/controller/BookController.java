package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String listBooks(Model model) {
        List<BookDTO> books = bookService.getAllBooks();

        Map<String, String> bookImages = new HashMap<>();
        for (BookDTO book : books) {
            String imagePath = "/images/booklabel.png"; // тестове зображення
            bookImages.put(book.getName(), imagePath);
        }

        model.addAttribute("books", books);
        model.addAttribute("bookImages", bookImages);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);

        return "books";
    }

    @GetMapping("/{name}")
    public String getBook(Model model,
                          @PathVariable String name) {
        BookDTO retrievedBook = bookService.getBookByName(name);
        model.addAttribute("book", retrievedBook);
        String bookImageUrl = bookService.getImageUrlByName(name);
        model.addAttribute("bookImage", bookImageUrl);
        return "book-details";
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
