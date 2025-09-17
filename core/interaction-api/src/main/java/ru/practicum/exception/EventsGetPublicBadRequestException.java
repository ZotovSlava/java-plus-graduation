package ru.practicum.exception;

public class EventsGetPublicBadRequestException extends RuntimeException {
    public EventsGetPublicBadRequestException() {
        super("Events not found.");
    }
}
