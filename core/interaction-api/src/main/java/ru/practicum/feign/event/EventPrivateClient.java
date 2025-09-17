package ru.practicum.feign.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.RequestEventDto;

import java.util.List;

//@FeignClient(name = "event", contextId = "eventPrivateClient", path = "/users")
public interface EventPrivateClient {
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
