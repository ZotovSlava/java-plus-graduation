package ru.practicum.service;

import ru.practicum.dto.event.*;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.grpc.recommendations.RecommendedEventProto;
import ru.practicum.model.AdminEventParams;
import ru.practicum.model.PrivateEventParams;
import ru.practicum.model.PublicEventParams;

import java.util.List;
import java.util.Set;

public interface EventService {
    List<EventFullDto> getAdmin(AdminEventParams params);

    List<EventShortDto> getPublic(PublicEventParams params);

    List<EventShortDto> getPrivate(PrivateEventParams params);

    EventFullDto getByIdPublic(Long userId, Long eventId, PublicEventParams params);

    EventFullDto getByIdPrivate(Long userId, Long eventId);

    SimpleEventDto getByIdAdmin(Long eventId);

    EventFullDto update(Long eventId, EventUpdateAdminDto eventDto);

    EventFullDto updatePrivate(Long userId, Long eventId, EventUpdateUserDto eventUpdateDto);

    EventFullDto updateConfirmedRequest(EventUpdateAdminDto eventUpdateDto, Long eventId);

    EventFullDto create(Long userId, EventCreateDto eventDto);

    List<RequestDto> getRequestsByIdPrivate(Long userId, Long eventId);

    EventResultRequestStatusDto updateRequestStatusPrivate(Long userId, Long eventId, EventUpdateRequestStatusDto updateDto);

    boolean hasEventsWithCategory(Long catId);

    List<EventFullDto> getAllByEventsId(List<Long> ids);

    EventFullDto createLike(Long eventId, Long userId);

    List<RecommendedEventProto> getInteractions(Set<Long> eventsIds);

    List<EventFullDto> getSimilarEvents(Long eventId, Long userId, int maxResults);

    List<EventFullDto> getRecommendations(Long userId, int maxResults);
}
