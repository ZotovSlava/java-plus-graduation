package ru.practicum.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RequestCreateDto {
    private LocalDateTime created;

    private Long requesterId;

    private Long eventId;

    private RequestStatus status;
}
