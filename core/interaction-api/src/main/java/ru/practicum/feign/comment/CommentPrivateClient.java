package ru.practicum.feign.comment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDtoRequest;
import ru.practicum.dto.comment.CommentDtoResponse;

import java.util.List;

//@FeignClient(name = "comment",    contextId = "commentPrivateClient", path = "/users/{userId}/events/{eventId}/comments")
public interface CommentPrivateClient {
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
