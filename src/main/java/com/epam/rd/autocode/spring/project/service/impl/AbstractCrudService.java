package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.mapper.BaseMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public record AbstractCrudService<E, D, K>(
        JpaRepository<E, ?> jpaRepository,
        BaseMapper<E, D> mapper,
        Function<K, E> finder,
        Predicate<D> existsPredicate,
        Function<D, String> messageFunction
) {
    public List<D> getAll() {
        return mapper.toDtoList(jpaRepository.findAll());
    }

    public D getByBusinessKey(K key) {
        E entity = finder.apply(key);
        return mapper.toDto(entity);
    }

    public D add(D dto) {
        if (existsPredicate != null && existsPredicate.test(dto)) {
            String message = messageFunction != null
                    ? messageFunction.apply(dto)
                    : "Entity already exists";
            throw new AlreadyExistException(message);
        }

        E entity = mapper.toEntity(dto);
        return mapper.toDto(jpaRepository.save(entity));
    }

    public D update(K id, D dto) {
        E existing = finder.apply(id);
        mapper.updateEntity(dto, existing);
        return mapper.toDto(jpaRepository.save(existing));
    }

    public void delete(K id) {
        jpaRepository.delete(finder.apply(id));
    }

    protected E findOrThrow(K id) {
        return finder.apply(id);
    }
}
