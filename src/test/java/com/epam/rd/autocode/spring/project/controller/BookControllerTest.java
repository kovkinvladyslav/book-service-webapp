package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookControllerTest {

    private BookService bookService;
    private BookController controller;

    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);
        controller = new BookController(bookService);
    }

    @Test
    void listBooks_populatesModel_andReturnsView() {
        BookFilterDTO filter = new BookFilterDTO();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        BookDTO b1 = new BookDTO(); b1.setName("A");
        Page<BookDTO> page = new PageImpl<>(List.of(b1));

        when(bookService.searchBookWithPaginationSortingAndFiltering(eq(filter), eq(pageable), eq("q")))
                .thenReturn(page);
        when(bookService.getBooksGenres()).thenReturn(List.of("Fantasy", "Sci-Fi"));

        Model model = new ExtendedModelMap();
        String view = controller.listBooks(filter, "q", pageable, model);

        assertThat(view).isEqualTo("books");
        assertThat(model.getAttribute("books")).isEqualTo(page);
        assertThat(model.getAttribute("genres")).isEqualTo(List.of("Fantasy", "Sci-Fi"));
        assertThat(model.getAttribute("ageGroups")).isEqualTo(AgeGroup.values());
        assertThat(model.getAttribute("languages")).isEqualTo(Language.values());
    }

    @Test
    void showAddBookForm_setsListsAndReturnsView() {
        when(bookService.getBooksGenres()).thenReturn(List.of("Drama"));

        Model model = new ExtendedModelMap();
        String view = controller.showAddBookForm(model);

        assertThat(view).isEqualTo("book-add");
        assertThat(model.getAttribute("bookDTO")).isInstanceOf(BookDTO.class);
        assertThat(model.getAttribute("genres")).isEqualTo(List.of("Drama"));
        assertThat(model.getAttribute("ageGroups")).isEqualTo(AgeGroup.values());
        assertThat(model.getAttribute("languages")).isEqualTo(Language.values());
    }

    @Test
    void addBook_whenValidationErrors_returnsFormWithLists() {
        when(bookService.getBooksGenres()).thenReturn(List.of("Drama"));
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        Model model = new ExtendedModelMap();
        String view = controller.addBook(new BookDTO(), br, model);

        assertThat(view).isEqualTo("book-add");
        assertThat(model.getAttribute("genres")).isEqualTo(List.of("Drama"));
        assertThat(model.getAttribute("ageGroups")).isEqualTo(AgeGroup.values());
        assertThat(model.getAttribute("languages")).isEqualTo(Language.values());

        verify(bookService).getBooksGenres();
        verify(bookService, never()).addBook(any(BookDTO.class));
        verifyNoMoreInteractions(bookService);
    }


    @Test
    void addBook_success_callsService_andRedirects() {
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        String view = controller.addBook(new BookDTO(), br, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/books");
        verify(bookService).addBook(any(BookDTO.class));
    }

    @Test
    void detailedBook_loadsFromService_andReturnsView() {
        BookDTO dto = new BookDTO(); dto.setName("Name");
        when(bookService.getBookByName("Name")).thenReturn(dto);

        Model model = new ExtendedModelMap();
        String view = controller.detailedBook("Name", model);

        assertThat(view).isEqualTo("book-details");
        assertThat(model.getAttribute("book")).isEqualTo(dto);
    }

    @Test
    void showEditForm_setsDtoAndLists_andReturnsView() {
        when(bookService.getBookByName("N")).thenReturn(new BookDTO());
        when(bookService.getBooksGenres()).thenReturn(List.of("G"));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm("N", model);

        assertThat(view).isEqualTo("book-edit");
        assertThat(model.getAttribute("bookDTO")).isInstanceOf(BookDTO.class);
        assertThat(model.getAttribute("genres")).isEqualTo(List.of("G"));
        assertThat(model.getAttribute("ageGroups")).isEqualTo(AgeGroup.values());
        assertThat(model.getAttribute("languages")).isEqualTo(Language.values());
    }

    @Test
    void updateBook_whenValidationErrors_returnsFormWithLists() {
        when(bookService.getBooksGenres()).thenReturn(List.of("G"));
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        Model model = new ExtendedModelMap();
        String view = controller.updateBook("Old", new BookDTO(), br, model);

        assertThat(view).isEqualTo("book-edit");
        assertThat(model.getAttribute("genres")).isEqualTo(List.of("G"));
        assertThat(model.getAttribute("ageGroups")).isEqualTo(AgeGroup.values());
        assertThat(model.getAttribute("languages")).isEqualTo(Language.values());
        verify(bookService, never()).updateBookByName(anyString(), any());
    }

    @Test
    void updateBook_success_redirectsToUpdatedName() {
        BookDTO updated = new BookDTO(); updated.setName("NewName");
        when(bookService.updateBookByName(eq("OldName"), any(BookDTO.class))).thenReturn(updated);

        String view = controller.updateBook("OldName", new BookDTO(), mock(BindingResult.class), new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/books/NewName");
    }

    @Test
    void deleteBook_callsService_andRedirects() {
        String view = controller.deleteBook("Name");
        assertThat(view).isEqualTo("redirect:/books");
        verify(bookService).deleteBookByName("Name");
    }

    @Test
    void restoreBook_callsService_andRedirects() {
        String view = controller.restoreBook("X");
        assertThat(view).isEqualTo("redirect:/books");
        verify(bookService).restoreBook("X");
    }
}
