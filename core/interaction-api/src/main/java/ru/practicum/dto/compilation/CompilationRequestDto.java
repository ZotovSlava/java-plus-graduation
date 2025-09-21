package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.dto.event.EventFullDto;

import java.util.List;

@Data
@AllArgsConstructor
public class CompilationRequestDto {
    private Long id;

    private String title;

    private Boolean pinned;

    private List<EventFullDto> events;
}
