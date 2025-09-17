package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.event.EventUpdateAdminDto;
import ru.practicum.dto.event.SimpleEventDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

//@FeignClient(name = "event", contextId = "eventAdminClient", path = "/admin/events")
public interface EventAdminClient {
    @PatchMapping("/admin/events/{id}")
    EventFullDto update(@RequestBody EventUpdateAdminDto eventUpdateDto,
                        @PathVariable("id") Long eventId);

    @GetMapping("/admin/events")
    List<EventFullDto> get(
            @RequestParam(required = false) Set<Long> users,
            @RequestParam(required = false) Set<EventState> states,
            @RequestParam(required = false) Set<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size
    );

    @GetMapping("/admin/events/{eventId}")
    SimpleEventDto getById(@PathVariable Long eventId);

    @GetMapping("/admin/events/exists-by-category")
    boolean hasEventsWithCategory(@RequestParam("catId") Long catId);

    @GetMapping("/admin/events/by-ids")
    List<EventFullDto> getAllByEventsId(@RequestParam List<Long> ids);
}
