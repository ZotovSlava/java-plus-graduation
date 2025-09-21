package ru.practicum.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long commentId) {
        super("Comment with id=" + commentId + " was not found");
    }
}
