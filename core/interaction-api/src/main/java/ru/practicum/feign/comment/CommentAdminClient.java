package ru.practicum.feign.comment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.comment.CommentDtoResponse;

import java.time.LocalDateTime;
import java.util.List;

//@FeignClient(name = "comment",    contextId = "commentAdminClient", path = "/admin/comments")
public interface CommentAdminClient {
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
}
