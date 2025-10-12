package com.epam.rd.autocode.spring.project.specification;

import com.epam.rd.autocode.spring.project.dto.BookFilterDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> getSpecification(BookFilterDTO filter, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(filter.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            if (hasText(filter.getGenre())) {
                predicates.add(cb.equal(cb.lower(root.get("genre")), filter.getGenre().toLowerCase()));
            }

            if (filter.getAgeGroup() != null) {
                predicates.add(cb.equal(root.get("ageGroup"), filter.getAgeGroup()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getPublicationDate() != null) {
                predicates.add(cb.equal(root.get("publicationDate"), filter.getPublicationDate()));
            }

            if (hasText(filter.getAuthor())) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + filter.getAuthor().toLowerCase() + "%"));
            }

            if (filter.getPages() != null) {
                predicates.add(cb.equal(root.get("pages"), filter.getPages()));
            }

            if (hasText(filter.getCharacteristics())) {
                predicates.add(cb.like(cb.lower(root.get("characteristics")), "%" + filter.getCharacteristics().toLowerCase() + "%"));
            }

            if (hasText(filter.getDescription())) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + filter.getDescription().toLowerCase() + "%"));
            }

            if (filter.getLanguage() != null) {
                predicates.add(cb.equal(root.get("language"), filter.getLanguage()));
            }

            if (hasText(search)) {
                String term = "%" + search.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), term);
                Predicate authorLike = cb.like(cb.lower(root.get("author")), term);
                Predicate genreLike = cb.like(cb.lower(root.get("genre")), term);
                Predicate descLike = cb.like(cb.lower(root.get("description")), term);
                predicates.add(cb.or(nameLike, authorLike, genreLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}