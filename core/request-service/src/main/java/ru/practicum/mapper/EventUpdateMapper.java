package ru.practicum.mapper;

import ru.practicum.dto.event.EventStateAction;
import ru.practicum.dto.event.EventUpdateAdminDto;
import ru.practicum.dto.event.SimpleEventDto;

public class EventUpdateMapper {
    public static EventUpdateAdminDto toEventUpdateAdminDto(SimpleEventDto simpleEventDto) {
        return EventUpdateAdminDto.builder()
                .annotation(simpleEventDto.getAnnotation())
                .category(simpleEventDto.getCategoryId())
                .description(simpleEventDto.getDescription())
                .eventDate(simpleEventDto.getEventDate())
                .location(simpleEventDto.getLocation())
                .paid(simpleEventDto.getPaid())
                .participantLimit(simpleEventDto.getParticipantLimit())
                .requestModeration(simpleEventDto.getRequestModeration())
                .stateAction(EventStateAction.PUBLISH_EVENT)
                .title(simpleEventDto.getTitle())
                .confirmedRequests(simpleEventDto.getConfirmedRequests())
                .build();
    }
}
