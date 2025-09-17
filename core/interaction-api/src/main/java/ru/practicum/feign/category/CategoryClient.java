package ru.practicum.feign.category;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryCreateDto;
import ru.practicum.dto.category.CategoryRequestDto;

import java.util.List;


@FeignClient(name = "category")
public interface CategoryClient {
    @GetMapping("/categories")
    List<CategoryRequestDto> get(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/categories/{catId}")
    CategoryRequestDto getById(@PathVariable("catId") Long catId);

    @PostMapping("/admin/categories")
    CategoryRequestDto create(@RequestBody CategoryCreateDto categoryCreateDto);

    @PatchMapping("/admin/categories/{catId}")
    CategoryRequestDto update(@RequestBody CategoryCreateDto categoryCreateDto,
                              @PathVariable("catId") Long catId);

    @DeleteMapping("/admin/categories/{catId}")
    void delete(@PathVariable("catId") Long catId);
}
