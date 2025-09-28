package ru.practicum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicSort;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.grpc.recommendations.RecommendedEventProto;
import ru.practicum.model.PublicEventParams;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> get(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Set<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventPublicSort sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        PublicEventParams publicEventParams = new PublicEventParams();
        publicEventParams.setText(text);
        publicEventParams.setCategories(categories);
        publicEventParams.setPaid(paid);
        publicEventParams.setRangeStart(rangeStart);
        publicEventParams.setRangeEnd(rangeEnd);
        publicEventParams.setOnlyAvailable(onlyAvailable);
        publicEventParams.setSort(sort);
        publicEventParams.setFrom(from);
        publicEventParams.setSize(size);
        publicEventParams.setIpAdr(request.getRemoteAddr());
        log.info("--> GET запрос /events с параметрами {}", publicEventParams);
        List<EventShortDto> events = eventService.getPublic(publicEventParams);
        log.info("<-- GET запрос /events вернул ответ: {}", events);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getById(
            @RequestHeader("X-EWM-USER-ID") Long userId,
            @PathVariable("id") Long eventId,
            HttpServletRequest request
    ) {
        PublicEventParams publicEventParams = new PublicEventParams();
        publicEventParams.setIpAdr(request.getRemoteAddr());
        log.info("--> GET запрос /events/{}", eventId);
        EventFullDto event = eventService.getByIdPublic(userId, eventId, publicEventParams);
        log.info("<-- GET запрос /events/{} вернул ответ: {}", eventId, event);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(event);
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<EventFullDto> createLike(@RequestHeader("X-EWM-USER-ID") long userId,
                                                @PathVariable("id") Long eventId) {
        log.info("--> PUT запрос /events/{}/like для пользователя {}", userId);
        EventFullDto event = eventService.createLike(eventId, userId);
        log.info("<-- PUT запрос /events/{}/like вернул ответ: {}", eventId, event);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(event);
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<EventFullDto>> getSimilarEvents(@RequestHeader("X-EWM-USER-ID") long userId,
                                                               @PathVariable("id") Long eventId,
                                                               @RequestParam(defaultValue = "10") int maxResults) {
        log.info("--> GET запрос /events/{}/similar для пользователя {}", userId);
        List<EventFullDto> events = eventService.getSimilarEvents(eventId, userId, maxResults);
        log.info("<-- GET запрос /events/{}/similar вернул ответ: {}", eventId, events);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(events);
    }

    @GetMapping("/recommendations")
    public  ResponseEntity<List<EventFullDto>> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId,
                                                                  @RequestParam(defaultValue = "10") int maxResults) {
        log.info("--> GET запрос /events/recommendations для пользователя {}", userId);
        List<EventFullDto> events = eventService.getRecommendations(userId, maxResults);
        log.info("<-- GET запрос /events/recommendations вернул ответ: {}", events);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(events);

    }

    @GetMapping("/interactions")
    public  ResponseEntity<List<Double>> getInteractions(@RequestParam(required = false) Set<Long> eventsIds) {
        log.info("--> GET запрос /events/interactions?eventsIds={}", eventsIds);
        List<RecommendedEventProto> results = eventService.getInteractions(eventsIds);
        log.info("<-- GET запрос /events/interactions?eventsIds={} вернул ответ: {}", eventsIds, results);
        List<Double> scores = results.stream()
                .map(RecommendedEventProto::getScore)
                .toList();
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(scores);

    }

}
