package ru.practicum.feign.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.RequestEventDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/events")
    List<EventShortDto> get(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Set<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventPublicSort sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request);

    @GetMapping("/events/{id}")
    EventFullDto getById(
            @PathVariable("id") Long eventId,
            HttpServletRequest request
    );

    @PatchMapping("/admin/events/{id}")
    EventFullDto update(@RequestBody EventUpdateAdminDto eventUpdateDto,
                        @PathVariable("id") Long eventId);

    @PostMapping("/admin/events/{id}/confirmedRequest")
    EventFullDto updateConfirmedRequest(@RequestBody EventUpdateAdminDto eventUpdateDto,
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

    @GetMapping("/users/{userId}/events")
    List<EventShortDto> get(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size);

    @PostMapping("/users/{userId}/events")
    EventFullDto create(@PathVariable("userId") Long userId,
                        @RequestBody EventCreateDto eventDto);

    @GetMapping("/users/{userId}/events/{eventId}")
    EventFullDto getById(@PathVariable("userId") Long userId,
                         @PathVariable("eventId") Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventFullDto update(@RequestBody EventUpdateUserDto eventUpdateDto,
                        @PathVariable("userId") Long userId,
                        @PathVariable("eventId") Long eventId);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<RequestEventDto> getAllRequests(@PathVariable("userId") Long userId,
                                         @PathVariable("eventId") Long eventId);


    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventResultRequestStatusDto update(@RequestBody EventUpdateRequestStatusDto updateDto,
                                       @PathVariable("userId") Long userId,
                                       @PathVariable("eventId") Long eventId);
}
