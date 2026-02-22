package com.jongsoft.finance.classification.domain.jpa.mapper;

import com.jongsoft.finance.classification.domain.jpa.entity.CategoryJpa;
import com.jongsoft.finance.classification.domain.model.Category;

import io.micronaut.context.annotation.Mapper;

public interface CategoryMapper {

    @Mapper.Mapping(to = "delete", from = "archived")
    Category toDomain(CategoryJpa entity);
}
