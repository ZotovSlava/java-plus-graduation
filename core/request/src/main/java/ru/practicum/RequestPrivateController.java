package ru.practicum;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.service.RequestService;

import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<RequestDto> create(@PathVariable Long userId,
                                             @RequestParam Long eventId) {
        RequestDto requestDto = requestService.create(userId, eventId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);
    }

    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<List<RequestDto>> get(@PathVariable Long userId) {
        List<RequestDto> requests = requestService.get(userId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requests);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> update(@PathVariable Long userId,
                                             @PathVariable Long requestId) {
        RequestDto requestDto = requestService.update(userId, requestId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);
    }

    @PutMapping("/users/{userId}/requests/{requestId}/state")
    public ResponseEntity<RequestDto> updateApprove(@PathVariable("userId") Long userId,
                                                    @PathVariable("requestId") Long requestId,
                                                    @RequestParam RequestStatus status) {
        RequestDto requestDto = requestService.updateState(userId, requestId, status);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<RequestDto> getAllByEventId(@PathVariable Long eventId) {
        return requestService.getAllByEventId(eventId);
    }

    @GetMapping("/events/{eventId}/requests/filter")
    public List<RequestDto> getAllByEventIdAndRequestIds(@PathVariable Long eventId,
                                                         @RequestParam Set<Long> requestIds) {
        return requestService.getAllByEventIdAndRequestIds(eventId, requestIds);
    }
}
