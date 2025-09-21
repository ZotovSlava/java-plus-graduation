package ru.practicum.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(Long eventId) {
        super("Event with id=" + eventId + " was not found");
    }

    public EventNotFoundException(String message) {
        super(message);
    }
}
