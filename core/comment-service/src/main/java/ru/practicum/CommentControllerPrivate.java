package ru.practicum;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDtoRequest;
import ru.practicum.dto.comment.CommentDtoResponse;
import ru.practicum.service.CommentService;


import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentControllerPrivate {

    final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDtoResponse create(@PathVariable Long userId, @PathVariable Long eventId,
                                     @Valid @RequestBody CommentDtoRequest dto) {
        return commentService.create(userId, eventId, dto);
    }

    @PatchMapping("/{commId}")
    public CommentDtoResponse update(@PathVariable Long userId, @PathVariable Long eventId, @PathVariable Long commId,
                                     @Valid @RequestBody CommentDtoRequest dto) {
        return commentService.update(userId, eventId, commId, dto);
    }

    @DeleteMapping("/{commId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long eventId, @PathVariable Long commId) {
        commentService.delete(userId, eventId, commId);
    }

    @GetMapping
    public List<CommentDtoResponse> findCommentsByUserIdAndEventId(@PathVariable Long userId,
                                                                   @PathVariable Long eventId) {
        return commentService.findCommentsByUserIdAndEventId(userId, eventId);
    }

}
