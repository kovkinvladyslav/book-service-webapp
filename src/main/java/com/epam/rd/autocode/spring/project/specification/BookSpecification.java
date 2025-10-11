package com.epam.rd.autocode.spring.project.specification;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> getSpecification(BookDTO dto, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(dto.getName())) predicates.add(cb.like(cb.lower(root.get("name")), "%" + dto.getName().toLowerCase() + "%"));
            if (hasText(dto.getGenre())) predicates.add(cb.equal(cb.lower(root.get("genre")), dto.getGenre().toLowerCase()));
            if (dto.getAgeGroup() != null) predicates.add(cb.equal(root.get("ageGroup"), dto.getAgeGroup()));
            if (dto.getPrice() != null) predicates.add(cb.equal(root.get("price"), dto.getPrice()));
            if (dto.getPublicationDate() != null) predicates.add(cb.equal(root.get("publicationDate"), dto.getPublicationDate()));
            if (hasText(dto.getAuthor())) predicates.add(cb.like(cb.lower(root.get("author")), "%" + dto.getAuthor().toLowerCase() + "%"));
            if (dto.getPages() != null) predicates.add(cb.equal(root.get("pages"), dto.getPages()));
            if (hasText(dto.getCharacteristics())) predicates.add(cb.like(cb.lower(root.get("characteristics")), "%" + dto.getCharacteristics().toLowerCase() + "%"));
            if (hasText(dto.getDescription())) predicates.add(cb.like(cb.lower(root.get("description")), "%" + dto.getDescription().toLowerCase() + "%"));
            if (dto.getLanguage() != null) predicates.add(cb.equal(root.get("language"), dto.getLanguage()));

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
