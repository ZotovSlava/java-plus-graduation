package ru.practicum.service;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.dto.comment.CommentDtoRequest;
import ru.practicum.dto.comment.CommentDtoResponse;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.event.SimpleEventDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.exception.*;
import ru.practicum.feign.event.EventAdminClient;
import ru.practicum.feign.event.EventClient;
import ru.practicum.feign.user.UserClient;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.service.specification.DbCommentSpecification;
import ru.practicum.storage.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentServiceImpl implements CommentService {

    final CommentRepository commentRepository;
    final EventClient eventAdminClient;
    final UserClient userClient;

    @Override
    public CommentDtoResponse create(Long userId, Long eventId, CommentDtoRequest dto) {
        SimpleEventDto event;
        try {
            event = eventAdminClient.getById(eventId);
        } catch (FeignException.NotFound e) {
            throw new EventNotFoundException(eventId);
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventDateException("Event with id=" + eventId + " is not published");
        }

        UserRequestDto user = userClient.getById(userId);

        Comment comment = commentRepository.save(CommentMapper.toEntity(dto, event.getId(), user.getId(), LocalDateTime.now()));

        return CommentMapper.toDto(comment);
    }

    @Override
    public CommentDtoResponse update(Long userId, Long eventId, Long commId, CommentDtoRequest dto) {
        Comment comment = getValidComment(userId, eventId, commId);
        comment.setText(dto.getText());
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void delete(Long userId, Long eventId, Long commId) {
        getValidComment(userId, eventId, commId);
        commentRepository.deleteById(commId);
    }

    @Override
    public CommentDtoResponse findCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        return CommentMapper.toDto(comment);
    }

    @Override
    public List<CommentDtoResponse> findCommentsByEventId(Long eventId) {
        eventAdminClient.getById(eventId);

        return CommentMapper.toDto(commentRepository.findAllByEventId(eventId));
    }

    @Override
    public List<CommentDtoResponse> findCommentsByUserIdAndEventId(Long userId, Long eventId) {
        eventAdminClient.getById(eventId);
        userClient.getById(userId);

        return CommentMapper.toDto(commentRepository.findAllByUserIdAndEventId(userId, eventId));
    }

    @Override
    public List<CommentDtoResponse> findCommentsAdmin(List<Integer> users, List<Integer> events,
                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                      Integer from, Integer size) {
        Specification<Comment> spec = DbCommentSpecification.getSpecificationAdmin(users, events, rangeStart, rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size);
        return CommentMapper.toDto(commentRepository.findAll(spec, pageable).getContent());
    }

    @Override
    public void deleteCommentAdmin(Long commId) {
        if (!commentRepository.existsById(commId)) {
            throw new CommentNotFoundException(commId);
        }
        commentRepository.deleteById(commId);
    }

    private Comment getValidComment(Long userId, Long eventId, Long commId) {
        eventAdminClient.getById(eventId);
        userClient.getById(userId);

        Comment comment = commentRepository.findById(commId)
                .orElseThrow(() -> new CommentNotFoundException(commId));

        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new ConflictException("User with id=" + userId + " is not the author of the comment");
        }

        return comment;
    }
}
