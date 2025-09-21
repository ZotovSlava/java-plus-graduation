package ru.practicum.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long catId) {
        super("Category with id=" + catId + " was not found");
    }
}
