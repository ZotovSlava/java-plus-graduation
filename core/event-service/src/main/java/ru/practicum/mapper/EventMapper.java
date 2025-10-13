package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryRequestDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.Event;

@Component
@RequiredArgsConstructor
public class EventMapper {

    public static EventFullDto toEventFullDto(Event event, CategoryRequestDto categoryRequestDto, UserRequestDto userRequestDto) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(categoryRequestDto);
        eventFullDto.setConfirmedRequests(event.getConfirmedRequests());
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setId(event.getId());
        eventFullDto.setInitiator(new UserShortDto(userRequestDto.getId(), userRequestDto.getName()));
        eventFullDto.setLocation(new Location(event.getLat(), event.getLon()));
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setRating(event.getRating());

        return eventFullDto;
    }

    public static EventShortDto toEventShortDto(Event event, CategoryRequestDto categoryRequestDto, UserRequestDto userRequestDto) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(categoryRequestDto);
        eventShortDto.setConfirmedRequests(event.getConfirmedRequests());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setId(event.getId());
        eventShortDto.setInitiator(new UserShortDto(userRequestDto.getId(), userRequestDto.getName()));
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setRating(event.getRating());

        return eventShortDto;
    }

    public static Event toEventFromUpdateAdmin(EventUpdateAdminDto eventDto, Long updCategory, Event curEvent) {

        if (eventDto.getAnnotation() != null) {
            curEvent.setAnnotation(eventDto.getAnnotation());
        }
        curEvent.setCategoryId(updCategory);
        if (eventDto.getDescription() != null) {
            curEvent.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            curEvent.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            curEvent.setLat(eventDto.getLocation().lat);
            curEvent.setLon(eventDto.getLocation().lon);
        }
        if (eventDto.getPaid() != null) {
            curEvent.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            curEvent.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
            curEvent.setState(EventState.PUBLISHED);
        }
        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
            curEvent.setState(EventState.CANCELED);
        }
        if (eventDto.getTitle() != null) {
            curEvent.setTitle(eventDto.getTitle());
        }

        return curEvent;
    }

    public static Event toEventFromUpdateUser(EventUpdateUserDto eventDto, Long updCategory, Event curEvent) {

        if (eventDto.getAnnotation() != null) {
            curEvent.setAnnotation(eventDto.getAnnotation());
        }
        curEvent.setCategoryId(updCategory);
        if (eventDto.getDescription() != null) {
            curEvent.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            curEvent.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            curEvent.setLat(eventDto.getLocation().lat);
            curEvent.setLon(eventDto.getLocation().lon);
        }
        if (eventDto.getParticipantLimit() != null) {
            curEvent.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(EventUserStateAction.SEND_TO_REVIEW)) {
            curEvent.setState(EventState.PENDING);
        }
        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(EventUserStateAction.CANCEL_REVIEW)) {
            curEvent.setState(EventState.CANCELED);
        }
        if (eventDto.getTitle() != null) {
            curEvent.setTitle(eventDto.getTitle());
        }

        return curEvent;
    }

    public static Event toEventFromCreatedDto(EventCreateDto eventDto, Long user, Long category) {
        Event event = new Event();
        event.setAnnotation(eventDto.getAnnotation());
        event.setCategoryId(category);
        event.setDescription(eventDto.getDescription());
        event.setCreatedOn(eventDto.getCreated());
        event.setEventDate(eventDto.getEventDate());
        event.setLat(eventDto.getLocation().lat);
        event.setLon(eventDto.getLocation().lon);
        event.setPaid(eventDto.getPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration());
        event.setInitiatorId(user);
        event.setTitle(eventDto.getTitle());
        event.setConfirmedRequests(0);
        event.setState(EventState.PENDING);
        event.setRating(event.getRating());

        return event;
    }

    public static SimpleEventDto toSimpleEventDto(Event event) {
        SimpleEventDto simpleEventDto = new SimpleEventDto();
        simpleEventDto.setAnnotation(event.getAnnotation());
        simpleEventDto.setCategoryId(event.getCategoryId());
        simpleEventDto.setConfirmedRequests(event.getConfirmedRequests());
        simpleEventDto.setCreatedOn(event.getCreatedOn());
        simpleEventDto.setDescription(event.getDescription());
        simpleEventDto.setEventDate(event.getEventDate());
        simpleEventDto.setId(event.getId());
        simpleEventDto.setInitiatorId(event.getInitiatorId());
        simpleEventDto.setLocation(new Location(event.getLat(), event.getLon()));
        simpleEventDto.setPaid(event.getPaid());
        simpleEventDto.setParticipantLimit(event.getParticipantLimit());
        simpleEventDto.setPublishedOn(event.getPublishedOn());
        simpleEventDto.setRequestModeration(event.getRequestModeration());
        simpleEventDto.setState(event.getState());
        simpleEventDto.setTitle(event.getTitle());
        simpleEventDto.setRating(event.getRating());

        return simpleEventDto;
    }

}
