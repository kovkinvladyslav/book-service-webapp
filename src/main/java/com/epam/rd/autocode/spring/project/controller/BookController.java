package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String listBooks(
            @ModelAttribute BookFilterDTO filter,
            @RequestParam(required = false) String searchQuery,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable,
            Model model
    ) {
        Page<BookDTO> books = bookService.searchBookWithPaginationSortingAndFiltering(
                filter, pageable, searchQuery);

        List<String> genres = bookService.getBooksGenres();

        model.addAttribute("books", books);
        model.addAttribute("genres", genres);
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());

        return "books";
    }

    @GetMapping("/manage/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("bookDTO", new BookDTO());
        model.addAttribute("genres", bookService.getBooksGenres());
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());
        return "book-add";
    }

    @PostMapping("/manage/add")
    public String addBook(@Valid @ModelAttribute BookDTO bookDTO,
                          BindingResult bindingResult,
                          Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", bookService.getBooksGenres());
            model.addAttribute("ageGroups", AgeGroup.values());
            model.addAttribute("languages", Language.values());
            return "book-add";
        }
        bookService.addBook(bookDTO);
        return "redirect:/books";
    }

    @GetMapping("/{name}")
    public String detailedBook(@PathVariable String name, Model model) {
        BookDTO bookDTO = bookService.getBookByName(name);
        model.addAttribute("book", bookDTO);
        return "book-details";
    }

    @GetMapping("/{name}/edit")
    public String showEditForm(@PathVariable String name, Model model) {
        BookDTO book = bookService.getBookByName(name);
        model.addAttribute("bookDTO", book);
        model.addAttribute("genres", bookService.getBooksGenres());
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());
        return "book-edit";
    }

    @PostMapping("/{name}/edit")
    public String updateBook(@PathVariable String name,
                             @Valid @ModelAttribute BookDTO bookDTO,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", bookService.getBooksGenres());
            model.addAttribute("ageGroups", AgeGroup.values());
            model.addAttribute("languages", Language.values());
            return "book-edit";
        }

        BookDTO updated = bookService.updateBookByName(name, bookDTO);

        return "redirect:/books/" + updated.getName();
    }

    @PostMapping("/{name}/delete")
    public String deleteBook(@PathVariable String name) {
        bookService.deleteBookByName(name);
        return "redirect:/books";
    }


    @PostMapping("/{name}/restore")
    public String restoreBook(@PathVariable String name) {
        bookService.restoreBook(name);
        return "redirect:/books";
    }
}