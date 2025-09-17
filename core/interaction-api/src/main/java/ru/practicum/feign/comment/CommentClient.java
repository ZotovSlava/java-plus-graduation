package ru.practicum.feign.comment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDtoRequest;
import ru.practicum.dto.comment.CommentDtoResponse;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "comment")
public interface CommentClient {
    @GetMapping("/comments/{commentId}")
    CommentDtoResponse findCommentById(@PathVariable("commentId") Long commentId);

    @GetMapping("/comments/events/{eventId}")
    List<CommentDtoResponse> findCommentsByEventId(@PathVariable("eventId") Long eventId);

    @GetMapping("/admin/comments")
    List<CommentDtoResponse> findCommentsAdmin(@RequestParam(required = false) List<Integer> users,
                                               @RequestParam(required = false) List<Integer> events,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               LocalDateTime rangeStart,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size);

    @DeleteMapping("/admin/comments/{commentId}")
    void deleteCommentAdmin(@PathVariable("commentId") Long commentId);

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    CommentDtoResponse create(@PathVariable("userId") Long userId,
                              @PathVariable("eventId") Long eventId,
                              @RequestBody CommentDtoRequest dto);

    @PatchMapping("/users/{userId}/events/{eventId}/comments/{commId}")
    CommentDtoResponse update(@PathVariable("userId") Long userId,
                              @PathVariable("eventId") Long eventId,
                              @PathVariable("commId") Long commId,
                              @RequestBody CommentDtoRequest dto);

    @DeleteMapping("/users/{userId}/events/{eventId}/comments/{commId}")
    void delete(@PathVariable("userId") Long userId,
                @PathVariable("eventId") Long eventId,
                @PathVariable("commId") Long commId);

    @GetMapping("/users/{userId}/events/{eventId}/comments")
    List<CommentDtoResponse> findCommentsByUserIdAndEventId(@PathVariable("userId") Long userId,
                                                            @PathVariable("eventId") Long eventId);
}
