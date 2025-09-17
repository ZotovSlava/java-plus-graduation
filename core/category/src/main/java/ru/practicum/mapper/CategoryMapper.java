package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryCreateDto;
import ru.practicum.dto.category.CategoryRequestDto;
import ru.practicum.model.Category;

@Component
public class CategoryMapper {
    public static Category toEntity(CategoryCreateDto categoryCreateDto) {
        Category category = new Category();
        category.setName(categoryCreateDto.getName());

        return category;
    }

    public static CategoryRequestDto toRequestDto(Category category) {
        return new CategoryRequestDto(
                category.getId(),
                category.getName()
        );
    }
}
