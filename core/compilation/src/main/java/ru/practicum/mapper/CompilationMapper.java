package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.compilation.CompilationCreateDto;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.model.Compilation;

import java.util.List;

@Component
public class CompilationMapper {
    public static Compilation toEntity(CompilationCreateDto compilationCreateDto, List<Long> events) {
        Compilation compilation = new Compilation();
        compilation.setTitle(compilationCreateDto.getTitle());
        compilation.setPinned(compilationCreateDto.getPinned());
        compilation.setEvents(events);

        return compilation;
    }

    public static CompilationRequestDto toRequestDto(Compilation compilation, List<EventFullDto> events) {
        return new CompilationRequestDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                events
        );
    }
}
