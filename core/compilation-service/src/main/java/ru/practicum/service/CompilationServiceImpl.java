package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationCreateDto;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.dto.compilation.CompilationUpdateDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.exception.CompilationNotFoundException;
import ru.practicum.feign.event.EventAdminClient;
import ru.practicum.feign.event.EventClient;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.storage.CompilationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventAdminClient;

    @Override
    public CompilationRequestDto create(CompilationCreateDto compilationCreateDto) {
        if (compilationCreateDto.getPinned() == null) {
            compilationCreateDto.setPinned(false);
        }

        List<Long> eventIds = compilationCreateDto.getEvents();
        if (eventIds == null) {
            eventIds = Collections.emptyList();
        } else {
            eventIds = eventIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        List<EventFullDto> events = eventAdminClient.getAllByEventsId(eventIds);

        return CompilationMapper.toRequestDto(
                compilationRepository.save(CompilationMapper.toEntity(compilationCreateDto, eventIds)),
                events
        );
    }

    @Override
    public CompilationRequestDto update(CompilationUpdateDto compilationUpdateDto, Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));

        if (compilationUpdateDto.getTitle() != null) {
            compilation.setTitle(compilationUpdateDto.getTitle());
        }

        if (compilationUpdateDto.getPinned() != null) {
            compilation.setPinned(compilationUpdateDto.getPinned());
        }

        List<Long> eventIds = compilationUpdateDto.getEvents();
        if (eventIds == null) {
            eventIds = Collections.emptyList();
        } else {
            eventIds = eventIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        List<EventFullDto> events = eventAdminClient.getAllByEventsId(eventIds);
        compilation.setEvents(eventIds);

        return CompilationMapper.toRequestDto(
                compilationRepository.save(compilation),
                events
        );
    }

    @Override
    public void delete(Long compId) {
        compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));

        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationRequestDto> get(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Page<Compilation> page;

        if (pinned == null) {
            page = compilationRepository.findAll(pageable);
        } else {
            page = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return page.stream()
                .map(c -> {
                    List<EventFullDto> events = eventAdminClient.getAllByEventsId(c.getEvents());
                    return CompilationMapper.toRequestDto(c, events);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationRequestDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));

        List<EventFullDto> events = eventAdminClient.getAllByEventsId(compilation.getEvents());

        return CompilationMapper.toRequestDto(compilation, events);
    }
}
