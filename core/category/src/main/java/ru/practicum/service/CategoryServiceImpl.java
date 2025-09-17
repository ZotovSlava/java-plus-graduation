package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryCreateDto;
import ru.practicum.dto.category.CategoryRequestDto;
import ru.practicum.exception.CategoryNotFoundException;
import ru.practicum.exception.ConflictException;
import ru.practicum.feign.event.EventAdminClient;
import ru.practicum.feign.event.EventClient;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.storage.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventClient eventAdminClient;

    @Override
    public CategoryRequestDto create(CategoryCreateDto categoryCreateDto) {
        return CategoryMapper.toRequestDto(
                categoryRepository.save(
                        CategoryMapper.toEntity(categoryCreateDto)
                )
        );
    }

    @Override
    public CategoryRequestDto update(CategoryCreateDto categoryCreateDto, Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));

        category.setName(categoryCreateDto.getName());

        return CategoryMapper.toRequestDto(
                categoryRepository.save(category)
        );
    }

    @Override
    public void delete(Long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));

        if (eventAdminClient.hasEventsWithCategory(catId)) {
                throw  new ConflictException("The category is not empty");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryRequestDto> get(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Page<Category> page = categoryRepository.findAll(pageable);

        return page.stream()
                .map(CategoryMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryRequestDto getById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));

        return CategoryMapper.toRequestDto(
                category
        );
    }
}
