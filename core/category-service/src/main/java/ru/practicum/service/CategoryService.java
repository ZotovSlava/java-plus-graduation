package ru.practicum.service;

import ru.practicum.dto.category.CategoryCreateDto;
import ru.practicum.dto.category.CategoryRequestDto;

import java.util.List;

public interface CategoryService {
    CategoryRequestDto create(CategoryCreateDto categoryCreateDto);

    CategoryRequestDto update(CategoryCreateDto categoryCreateDto, Long catId);

    void delete(Long catId);

    List<CategoryRequestDto> get(int from, int size);

    CategoryRequestDto getById(Long catId);

}
