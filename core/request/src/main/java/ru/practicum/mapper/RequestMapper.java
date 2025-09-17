package ru.practicum.mapper;

import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestEventDto;
import ru.practicum.model.Request;

public class RequestMapper {
    public static RequestDto toDto(Request request) {
        return new RequestDto(
                request.getId(),
                request.getCreated(),
                request.getRequesterId(),
                request.getEventId(),
                request.getStatus()
        );
    }

    public static RequestEventDto toEventRequestDto(Request request) {
        return new RequestEventDto(
                request.getId(),
                request.getCreated(),
                request.getRequesterId(),
                request.getEventId(),
                request.getStatus()
        );
    }

}
