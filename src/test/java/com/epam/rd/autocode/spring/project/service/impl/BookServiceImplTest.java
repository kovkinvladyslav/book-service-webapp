package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.GenericMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceImplTest {

    private BookRepository bookRepository;
    private GenericMapper<Book, BookDTO> bookMapper;
    private BookServiceImpl service;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        bookMapper = mock(GenericMapper.class);
        service = new BookServiceImpl(bookRepository, bookMapper);
    }

    @Test
    void getAllBooks_filtersDeleted() {
        Book a = new Book(); a.setDeleted(false);
        Book b = new Book(); b.setDeleted(true);
        when(bookRepository.findAll()).thenReturn(List.of(a,b));
        when(bookMapper.toDtoList(List.of(a))).thenReturn(List.of(new BookDTO()));

        assertThat(service.getAllBooks()).hasSize(1);
    }

    @Test
    void searchBookWithPaginationSortingAndFiltering_mapsPage() {
        BookFilterDTO filter = new BookFilterDTO();
        Pageable pageable = PageRequest.of(0,10);
        Book e = new Book();
        Page<Book> page = new PageImpl<>(List.of(e));
        when(bookRepository.findAll(
                ArgumentMatchers.<Specification<Book>>any(),
                eq(pageable))
        ).thenReturn(page);        BookDTO d = new BookDTO();
        when(bookMapper.toDto(e)).thenReturn(d);

        Page<BookDTO> out = service.searchBookWithPaginationSortingAndFiltering(filter, pageable, "q");
        assertThat(out.getContent()).containsExactly(d);
    }

    @Test
    void addBook_whenNameAlreadyExists_throwsAlreadyExist_andDoesNotSave() {
        BookDTO dto = new BookDTO();
        dto.setName("N"); // важливо: не null

        when(bookRepository.findByName("N")).thenReturn(java.util.Optional.of(new Book()));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.addBook(dto))
                .isInstanceOf(AlreadyExistException.class);

        verify(bookRepository, never()).save(any());
        verify(bookMapper, never()).toEntity(any());
    }


    @Test
    void getBooksGenres_returnsRepo() {
        when(bookRepository.findDistinctGenres()).thenReturn(List.of("A","B"));
        assertThat(service.getBooksGenres()).containsExactly("A","B");
    }

    @Test
    void getBooksFromBookItems_resolvesByName() {
        var dto = new BookDTO(); dto.setName("N");
        var item = new com.epam.rd.autocode.spring.project.dto.BookItemDTO();
        item.setBook(dto); item.setQuantity(2);
        Book b = new Book(); b.setName("N");
        BookDTO mapped = new BookDTO(); mapped.setName("N");
        when(bookRepository.findByName("N")).thenReturn(Optional.of(b));
        when(bookMapper.toDto(b)).thenReturn(mapped);

        var map = service.getBooksFromBookItems(List.of(item));
        assertThat(map).containsEntry(mapped, 2);
    }

    @Test
    void getEntityByName_found() {
        Book b = new Book();
        when(bookRepository.findByName("N")).thenReturn(Optional.of(b));
        assertThat(service.getEntityByName("N")).isSameAs(b);
    }

    @Test
    void getEntityByName_notFound_throws() {
        when(bookRepository.findByName("X")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getEntityByName("X")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void restoreBook_setsDeletedFalse_andSaves() {
        Book b = new Book(); b.setDeleted(true);
        when(bookRepository.findByName("N")).thenReturn(Optional.of(b));

        service.restoreBook("N");

        assertThat(b.isDeleted()).isFalse();
        verify(bookRepository).save(b);
    }

    @Test
    void getBookByName_maps() {
        Book b = new Book();
        BookDTO d = new BookDTO();
        when(bookRepository.findByName("N")).thenReturn(Optional.of(b));
        when(bookMapper.toDto(b)).thenReturn(d);
        assertThat(service.getBookByName("N")).isSameAs(d);
    }

    @Test
    void getBookByName_notFound_throws() {
        when(bookRepository.findByName("N")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getBookByName("N")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void addBook_whenExists_throws() {
        BookDTO dto = new BookDTO(); dto.setName("N");
        when(bookRepository.findByName("N")).thenReturn(Optional.of(new Book()));
        assertThatThrownBy(() -> service.addBook(dto)).isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void addBook_saves_andMaps() {
        BookDTO dto = new BookDTO(); dto.setName("N");
        Book ent = new Book();
        Book saved = new Book();
        BookDTO out = new BookDTO();
        when(bookRepository.findByName("N")).thenReturn(Optional.empty());
        when(bookMapper.toEntity(dto)).thenReturn(ent);
        when(bookRepository.save(ent)).thenReturn(saved);
        when(bookMapper.toDto(saved)).thenReturn(out);

        assertThat(service.addBook(dto)).isSameAs(out);
    }

    @Test
    void updateBookByName_notFound_throws() {
        when(bookRepository.findByName("old")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateBookByName("old", new BookDTO())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateBookByName_renameToExisting_throws() {
        BookDTO dto = new BookDTO(); dto.setName("new");
        Book existing = new Book();
        when(bookRepository.findByName("old")).thenReturn(Optional.of(existing));
        when(bookRepository.findByName("new")).thenReturn(Optional.of(new Book()));

        assertThatThrownBy(() -> service.updateBookByName("old", dto)).isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void updateBookByName_success_updatesAndSaves() {
        BookDTO dto = new BookDTO(); dto.setName("old");
        Book existing = new Book();
        Book saved = new Book();
        BookDTO mapped = new BookDTO();
        when(bookRepository.findByName("old")).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(saved);
        when(bookMapper.toDto(saved)).thenReturn(mapped);

        assertThat(service.updateBookByName("old", dto)).isSameAs(mapped);
        verify(bookMapper).updateEntity(dto, existing);
    }

    @Test
    void deleteBookByName_notFound_throws() {
        when(bookRepository.findByName("X")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteBookByName("X")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteBookByName_deletes() {
        Book b = new Book();
        when(bookRepository.findByName("N")).thenReturn(Optional.of(b));
        service.deleteBookByName("N");
        verify(bookRepository).delete(b);
    }
}
