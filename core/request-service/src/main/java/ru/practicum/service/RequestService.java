package ru.practicum.service;

import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestStatus;

import java.util.List;
import java.util.Set;

public interface RequestService {
    RequestDto create(Long userId, Long eventId);

    List<RequestDto> get(Long userId);

    RequestDto update(Long userId, Long requestId);

    List<RequestDto> getAllByEventId(Long eventId);

    List<RequestDto> getAllByEventIdAndRequestIds(Long eventId, Set<Long> requestIds);

    RequestDto updateState(Long userId, Long requestId, RequestStatus status);
}


