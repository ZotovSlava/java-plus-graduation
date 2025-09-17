package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.comment.CommentDtoRequest;
import ru.practicum.dto.comment.CommentDtoResponse;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public static CommentDtoResponse toDto(Comment comment) {
        return new CommentDtoResponse(
                comment.getId(),
                comment.getCreated(),
                comment.getEventId(),
                comment.getUserId(),
                comment.getText()
        );
    }

    public static List<CommentDtoResponse> toDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    public static Comment toEntity(CommentDtoRequest dto, Long eventId, Long userId, LocalDateTime createdEntity) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setEventId(eventId);
        comment.setUserId(userId);
        comment.setCreated(createdEntity);
        return comment;
    }
}
