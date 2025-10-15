package com.epam.rd.autocode.spring.project.logging;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CatalogAspect {

    @After("execution(* com.epam.rd.autocode.spring.project.service.BookService.addBook(..)) && args(dto)")
    public void afterAdd(BookDTO dto) {
        log.info("[BOOK] add name={} author={} price={}",
                dto != null ? dto.getName() : "null",
                dto != null ? dto.getAuthor() : "null",
                dto != null ? dto.getPrice() : "null");
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.BookService.updateBookByName(..)) && args(oldName, dto)")
    public void afterUpdate(String oldName, BookDTO dto) {
        log.info("[BOOK] update oldName={} newName={}", LogSanitizer.mask(oldName), dto != null ? dto.getName() : "null");
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.BookService.deleteBookByName(..)) && args(name)")
    public void afterDelete(String name) {
        log.warn("[BOOK] delete name={}", LogSanitizer.mask(name));
    }

    @After("execution(* com.epam.rd.autocode.spring.project.service.BookService.restoreBook(..)) && args(name)")
    public void afterRestore(String name) {
        log.info("[BOOK] restore name={}", LogSanitizer.mask(name));
    }
}
