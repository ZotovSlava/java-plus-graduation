package ru.practicum.model;

import lombok.Data;
import ru.practicum.dto.event.EventPublicSort;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PublicEventParams {
    private String text;
    private Set<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private EventPublicSort sort;
    private Integer from;
    private Integer size;
    private String ipAdr;
}
