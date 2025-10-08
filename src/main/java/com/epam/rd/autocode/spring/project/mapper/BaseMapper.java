package com.epam.rd.autocode.spring.project.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseMapper<E, D> {

    @Autowired
    protected ModelMapper modelMapper;

    private final Class<E> entityClass;
    private final Class<D> dtoClass;

    @SuppressWarnings("unchecked")
    protected BaseMapper() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<E>) type.getActualTypeArguments()[0];
        this.dtoClass = (Class<D>) type.getActualTypeArguments()[1];
    }

    public D toDto(E entity) {
        return modelMapper.map(entity, dtoClass);
    }

    public E toEntity(D dto) {
        return modelMapper.map(dto, entityClass);
    }

    public void updateEntity(D dto, E entity) {
        modelMapper.map(dto, entity);
    }

    public List<D> toDtoList(List<E> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
