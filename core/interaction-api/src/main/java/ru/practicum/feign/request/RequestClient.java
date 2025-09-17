package ru.practicum.feign.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestStatus;

import java.util.List;
import java.util.Set;

@FeignClient(name = "request")
public interface RequestClient {

    @PostMapping("/users/{userId}/requests")
    RequestDto create(@PathVariable("userId") Long userId,
                      @RequestParam Long eventId);

    @GetMapping("/users/{userId}/requests")
    List<RequestDto> get(@PathVariable("userId") Long userId);

    @GetMapping("/events/{eventId}/requests")
    List<RequestDto> getAllByEventId(@PathVariable("eventId") Long eventId);

    @GetMapping("/events/{eventId}/requests/filter")
    List<RequestDto> getAllByEventIdAndRequestIds(@PathVariable("eventId") Long eventId,
                                                  @RequestParam Set<Long> requestIds);

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    RequestDto update(@PathVariable("userId") Long userId,
                      @PathVariable("requestId") Long requestId);

    @PutMapping("/users/{userId}/requests/{requestId}/state")
    RequestDto updateState(@PathVariable("userId") Long userId,
                           @PathVariable("requestId") Long requestId,
                           @RequestParam("status") RequestStatus status);

}
