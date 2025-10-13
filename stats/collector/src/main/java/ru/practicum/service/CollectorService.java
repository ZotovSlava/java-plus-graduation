package ru.practicum.service;

import ru.practicum.dto.UserActionDto;

public interface CollectorService {
    void createUserAction(UserActionDto userActionDto);
}
