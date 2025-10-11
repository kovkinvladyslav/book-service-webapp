package com.epam.rd.autocode.spring.project.mapper;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

public final class GenericMapper<E, D> {

    private final ModelMapper modelMapper;
    private final Class<E> entityClass;
    private final Class<D> dtoClass;

    public GenericMapper(ModelMapper modelMapper, Class<E> entityClass, Class<D> dtoClass) {
        this.modelMapper = modelMapper;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
    }

    public D toDto(E entity) {
        if (entity == null) {
            return null;
        }
        return modelMapper.map(entity, dtoClass);
    }

    public E toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, entityClass);
    }

    public void updateEntity(D dto, E entity) {
        if (dto != null && entity != null) {
            modelMapper.map(dto, entity);
        }
    }

    public List<D> toDtoList(List<E> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}