package ru.practicum.service;


import ru.practicum.dto.user.UserCreateDto;
import ru.practicum.dto.user.UserRequestDto;

import java.util.List;

public interface UserService {
    UserRequestDto create(UserCreateDto userCreateDto);

    List<UserRequestDto> get(List<Integer> ids, int from, int size);

    UserRequestDto getById(Long userId);

    void delete(Long userId);
}
