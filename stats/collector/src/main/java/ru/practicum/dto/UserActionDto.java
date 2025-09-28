package ru.practicum.dto;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@SuperBuilder
@Getter
@ToString
public class UserActionDto {
    private Long userId;
    private Long eventId;
    ActionType actionType;
    private Instant timestamp;
}
