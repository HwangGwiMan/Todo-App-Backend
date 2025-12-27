package com.TodoApp.backend.global.common.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Generic Mapper Interface
 *
 * @param <D> Request DTO type
 * @param <R> Response DTO type
 * @param <E> Entity type
 */
public interface GenericMapper<D, R, E> {

    E toEntity(D request);

    R toDto(E entity);

    List<R> toDtoList(List<E> entityList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(D request, @MappingTarget E entity);
}

