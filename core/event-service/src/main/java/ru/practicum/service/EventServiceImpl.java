package ru.practicum.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatRestClient;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.dto.category.CategoryRequestDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.exception.*;
import ru.practicum.feign.category.CategoryClient;
import ru.practicum.feign.request.RequestClient;
import ru.practicum.feign.user.UserClient;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.storage.EventRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
@ComponentScan(value = {"ru.yandex.practicum.ewm", "ru.practicum.client"})
public class EventServiceImpl implements EventService {
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 12, 31, 23, 59);

    private final EventRepository eventRepository;
    private final CategoryClient categoryClient;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final EventMapper mapper;

    StatRestClient statClient;

    @Override
    public List<EventFullDto> getAdmin(AdminEventParams params) {

        PageRequest pageRequest = PageRequest.of(
                (params.getFrom() != null && params.getFrom() > 0 ? params.getFrom() : 0)
                        / (params.getSize() != null && params.getSize() > 0 ? params.getSize() : 10),
                (params.getSize() != null && params.getSize() > 0 ? params.getSize() : 10),
                Sort.by("id").ascending()
        );

        BooleanExpression filter = byStates(params.getStates())
                .and(byCategoryIds(params.getCategories()))
                .and(byUserIds(params.getUsers()))
                .and(byDates(params.getRangeStart(), params.getRangeEnd()));

        Page<Event> pageEvents = eventRepository.findAll(filter, pageRequest);
        List<Event> foundEvents = pageEvents.getContent();

        return foundEvents.stream()
                .map(c -> {
                    log.info("Fetching category with id={} 🙂", c.getCategoryId());
                    CategoryRequestDto categoryRequestDto = categoryClient.getById(c.getCategoryId());
                    UserRequestDto userRequestDto = userClient.getById(c.getInitiatorId());
                    return EventMapper.toEventFullDto(c, categoryRequestDto, userRequestDto);

                })
                .toList();
    }

    @Override
    public List<EventShortDto> getPublic(PublicEventParams params) {

        PageRequest pageRequest;
        if (params.getSort() != null) {
            if (params.getSort().equals(EventPublicSort.EVENT_DATE)) {
                pageRequest = PageRequest.of(params.getFrom() > 0 ? params.getFrom() / params.getSize() : 0, params.getSize(), Sort.by("eventDate"));
            } else if (params.getSort().equals(EventPublicSort.VIEWS)) {
                pageRequest = PageRequest.of(params.getFrom() > 0 ? params.getFrom() / params.getSize() : 0, params.getSize(), Sort.by("views"));
            } else {
                pageRequest = PageRequest.of(params.getFrom() > 0 ? params.getFrom() / params.getSize() : 0, params.getSize());
            }
        } else {
            pageRequest = PageRequest.of(params.getFrom() > 0 ? params.getFrom() / params.getSize() : 0, params.getSize());
        }

        BooleanExpression filter = byPublishedEvents()
                .and(byText(params.getText()))
                .and(byCategoryIds(params.getCategories()))
                .and(byPaid(params.getPaid()))
                .and(byOnlyAvailable(params.getOnlyAvailable()))
                .and(byDatesWithDefaults(params.getRangeStart(), params.getRangeEnd()));

        Page<Event> pageEvents = eventRepository.findAll(filter, pageRequest);
        List<Event> foundEvents = pageEvents.getContent();
        if (foundEvents.isEmpty()) {
            throw new EventsGetPublicBadRequestException();
        }
        statClient.saveHit(new HitDto("ewm-main-service", "/events", params.getIpAdr(), LocalDateTime.now()));


        return foundEvents.stream()
                .map(c -> {
                    CategoryRequestDto categoryRequestDto = categoryClient.getById(c.getCategoryId());
                    UserRequestDto userRequestDto = userClient.getById(c.getInitiatorId());
                    return EventMapper.toEventShortDto(c, categoryRequestDto, userRequestDto);
                })
                .toList();
    }

    @Override
    public List<EventShortDto> getPrivate(PrivateEventParams params) {
        UserRequestDto user = userClient.getById(params.getUserId());

        PageRequest pageRequest = PageRequest.of(params.getFrom() > 0 ? params.getFrom() / params.getSize() : 0, params.getSize());
        BooleanExpression filter = byUserIds(Set.of(params.getUserId()));
        Page<Event> pageEvents = eventRepository.findAll(filter, pageRequest);
        List<Event> foundEvents = pageEvents.getContent();

        return foundEvents.stream()
                .map(c -> {
                    CategoryRequestDto categoryRequestDto = categoryClient.getById(c.getCategoryId());
                    UserRequestDto userRequestDto = userClient.getById(c.getInitiatorId());
                    return EventMapper.toEventShortDto(c, categoryRequestDto, userRequestDto);
                })
                .toList();
    }

    @Override
    public EventFullDto getByIdPublic(Long eventId, PublicEventParams params) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty() || !event.get().getState().equals(EventState.PUBLISHED)) {
            throw new EventNotFoundException(eventId);
        }
        List<StatsDto> stats = statClient.getStats("1900-01-01 00:00:00", "2100-01-01 00:00:00", List.of("/events/" + eventId), true);
        if (stats.isEmpty()) {
            event.get().setViews(event.get().getViews() + 1);
            eventRepository.save(event.get());
        }
        statClient.saveHit(new HitDto("ewm-main-service", "/events/" + eventId, params.getIpAdr(), LocalDateTime.now()));

        CategoryRequestDto categoryRequestDto = categoryClient.getById(event.get().getCategoryId());
        UserRequestDto userRequestDto = userClient.getById(event.get().getInitiatorId());

        return EventMapper.toEventFullDto(event.get(), categoryRequestDto, userRequestDto);
    }

    @Override
    public EventFullDto getByIdPrivate(Long userId, Long eventId) {
        UserRequestDto user = userClient.getById(userId);


        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EventNotFoundException(eventId);
        }
        if (!Objects.equals(event.get().getInitiatorId(), userId)) {
            throw new EventGetBadRequestException(eventId, userId);
        }
        CategoryRequestDto categoryRequestDto = categoryClient.getById(event.get().getCategoryId());

        return EventMapper.toEventFullDto(event.get(), categoryRequestDto, user);
    }

    @Override
    public SimpleEventDto getByIdAdmin(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return EventMapper.toSimpleEventDto(event);
    }

    @Override
    public EventFullDto update(Long eventId, EventUpdateAdminDto eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Получаем текущую категорию
        CategoryRequestDto category = categoryClient.getById(event.getCategoryId());

        // Если пришла новая категория, обновляем
        if (eventDto.getCategory() != null && !eventDto.getCategory().equals(event.getCategoryId())) {
            category = categoryClient.getById(eventDto.getCategory());
            event.setCategoryId(eventDto.getCategory());
        }

        // Обновляем остальные поля только если они пришли
        if (eventDto.getAnnotation() != null) event.setAnnotation(eventDto.getAnnotation());
        if (eventDto.getDescription() != null) event.setDescription(eventDto.getDescription());
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now())) {
                throw new EventDateException("Дата события не может быть в прошлом.");
            }
            if (event.getPublishedOn() != null &&
                    eventDto.getEventDate().isBefore(event.getPublishedOn().minus(1, ChronoUnit.HOURS))) {
                throw new DataIntegrityViolationException(
                        "Дата начала события должна быть не ранее чем за час от даты публикации."
                );
            }
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) event.setPaid(eventDto.getPaid());
        if (eventDto.getParticipantLimit() != null) event.setParticipantLimit(eventDto.getParticipantLimit());
        if (eventDto.getRequestModeration() != null) event.setRequestModeration(eventDto.getRequestModeration());
        if (eventDto.getTitle() != null) event.setTitle(eventDto.getTitle());

        // Работа с состоянием
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new DataIntegrityViolationException(
                            "Событие можно публиковать только если оно в состоянии ожидания публикации"
                    );
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new DataIntegrityViolationException(
                            "Событие можно отклонить, только если оно еще не опубликовано"
                    );
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event updEvent = eventRepository.save(event);
        UserRequestDto userRequestDto = userClient.getById(updEvent.getInitiatorId());

        return EventMapper.toEventFullDto(updEvent, category, userRequestDto);
    }



    @Override
    public EventFullDto updatePrivate(Long userId, Long eventId, EventUpdateUserDto eventDto) {
        UserRequestDto user = userClient.getById(userId);

        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EventNotFoundException(eventId);
        }

        if (eventDto.getCategory() != null && !eventDto.getCategory().equals(event.get().getCategoryId())) {
            CategoryRequestDto category = categoryClient.getById(eventDto.getCategory());
        }

        if (!Objects.equals(event.get().getInitiatorId(), userId)) {
            throw new EventGetBadRequestException(eventId, userId);
        }
        if (eventDto.getEventDate() != null && eventDto.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new EventDateException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента.");
        }
        if (event.get().getState().equals(EventState.PUBLISHED)) {
            throw new DataIntegrityViolationException("Изменить можно только отмененные события или события в состоянии ожидания модерации.");
        }
        Event updEvent = EventMapper.toEventFromUpdateUser(eventDto, event.get().getCategoryId(), event.get());
        updEvent = eventRepository.save(updEvent);
        CategoryRequestDto categoryRequestDto = categoryClient.getById(updEvent.getCategoryId());
        UserRequestDto userRequestDto = userClient.getById(updEvent.getInitiatorId());

        return EventMapper.toEventFullDto(updEvent, categoryRequestDto, userRequestDto);
    }

    @Override
    public EventFullDto updateConfirmedRequest(EventUpdateAdminDto eventDto, Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EventNotFoundException(eventId);
        }

        event.get().setConfirmedRequests(eventDto.getConfirmedRequests());

        Event updEvent = eventRepository.save(event.get());
        CategoryRequestDto categoryRequestDto = categoryClient.getById(updEvent.getCategoryId());
        UserRequestDto userRequestDto = userClient.getById(updEvent.getInitiatorId());

        return EventMapper.toEventFullDto(updEvent, categoryRequestDto, userRequestDto);
    }

    @Override
    public EventFullDto create(Long userId, EventCreateDto eventDto) {
        UserRequestDto user = userClient.getById(userId);

        CategoryRequestDto category = categoryClient.getById(eventDto.getCategory());

        Event event = EventMapper.toEventFromCreatedDto(eventDto, userId, category.getId());
        event = eventRepository.save(event);
        log.info("🙂🙂🙂🙂🙂🙂🙂🙂🙂🙂🙂 {}", event.toString());
        return EventMapper.toEventFullDto(event, category, user);
    }

    @Override
    public List<RequestDto> getRequestsByIdPrivate(Long userId, Long eventId) {
        UserRequestDto user = userClient.getById(userId);

        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EventNotFoundException(eventId);
        }

        if (!event.get().getInitiatorId().equals(userId)) {
            throw new ConflictException("Вы не являетесь владельцем данного события");
        }

        return requestClient.getAllByEventId(eventId);
    }

    @Override
    public EventResultRequestStatusDto updateRequestStatusPrivate(Long userId, Long eventId, EventUpdateRequestStatusDto updateDto) {

        UserRequestDto user = userClient.getById(userId);

        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EventNotFoundException(eventId);
        }
        Integer confReqs = event.get().getConfirmedRequests();
        Integer limit = event.get().getParticipantLimit();
        if (limit == 0 || !event.get().getRequestModeration()) {
            return null;
        }
        if (Objects.equals(confReqs, limit) && updateDto.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new DataIntegrityViolationException("The participant limit has been reached");
        }
        int count = limit - confReqs;
        int counter = 0;
        List<RequestDto> requests = requestClient.getAllByEventIdAndRequestIds(eventId, updateDto.getRequestIds());
        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();
        for (RequestDto request : requests) {
            log.info("🙂🙂🙂🙂🙂🙂🙂🙂🙂🙂🙂 {}", request.toString());
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new DataIntegrityViolationException("Request must have status PENDING");
            }
            if (updateDto.getStatus().equals(RequestStatus.CONFIRMED) && counter < count) {
                counter++;
                requestClient.updateState(userId, request.getId(), RequestStatus.CONFIRMED);
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
            } else {
                //counter++;
                requestClient.updateState(userId, request.getId(), RequestStatus.REJECTED);
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }
        event.get().setConfirmedRequests(confReqs + counter);
        eventRepository.save(event.get());

        EventResultRequestStatusDto results = new EventResultRequestStatusDto();
        results.setConfirmedRequests(confirmedRequests);
        results.setRejectedRequests(rejectedRequests);
        return results;
    }

    @Override
    public boolean hasEventsWithCategory(Long catId) {
        return eventRepository.findFirstByCategoryId(catId).isPresent();
    }

    @Override
    public List<EventFullDto> getAllByEventsId(List<Long> ids) {
        List<Event> events = eventRepository.findAllById(ids);

        if (ids.size() > events.size()) {
            throw new EventNotFoundException("Events not found");
        }

        return events.stream()
                .map(c -> {
                    CategoryRequestDto categoryRequestDto = categoryClient.getById(c.getCategoryId());
                    UserRequestDto userRequestDto = userClient.getById(c.getInitiatorId());
                    return EventMapper.toEventFullDto(c, categoryRequestDto, userRequestDto);
                })
                .toList();
    }

    private BooleanExpression byStates(Set<EventState> states) {
        Set<EventState> defaultStates = Set.of(EventState.CANCELED, EventState.PENDING, EventState.PUBLISHED);
        return QEvent.event.state.in(states != null && !states.isEmpty() ? states : defaultStates);
    }

    private BooleanExpression byCategoryIds(Set<Long> categories) {
        return (categories != null && !categories.isEmpty() && !categories.contains(0L))
                ? QEvent.event.categoryId.in(categories)
                : QEvent.event.categoryId.isNotNull();
    }

    private BooleanExpression byUserIds(Set<Long> users) {
        return (users != null && !users.isEmpty() && !users.contains(0L))
                ? QEvent.event.initiatorId.in(users)
                : QEvent.event.initiatorId.isNotNull();
    }

    private BooleanExpression byDates(LocalDateTime start, LocalDateTime end) {
        QEvent event = QEvent.event;
        LocalDateTime safeStart = (start != null && start.isAfter(MIN_DATE)) ? start : MIN_DATE;
        LocalDateTime safeEnd = (end != null && end.isBefore(MAX_DATE)) ? end : MAX_DATE;

        if (start != null && end != null) {
            return event.eventDate.between(safeStart, safeEnd);
        } else if (start != null) {
            return event.eventDate.after(safeStart);
        } else if (end != null) {
            return event.eventDate.before(safeEnd);
        } else {
            return Expressions.asBoolean(true).isTrue();
        }
    }

    private BooleanExpression byDatesWithDefaults(LocalDateTime start, LocalDateTime end) {
        LocalDateTime startDate = (start != null) ? start : LocalDateTime.now();
        if (end != null) {
            return QEvent.event.eventDate.after(startDate).and(QEvent.event.eventDate.before(end));
        } else {
            return QEvent.event.eventDate.after(startDate);
        }
    }

    private BooleanExpression byText(String text) {
        return (text != null && !text.isBlank() && !text.equals("0"))
                ? QEvent.event.annotation.containsIgnoreCase(text)
                : QEvent.event.annotation.isNotNull();
    }

    private BooleanExpression byPaid(Boolean paid) {
        return paid != null ? QEvent.event.paid.eq(paid) : QEvent.event.paid.isNotNull();
    }

    private BooleanExpression byPublishedEvents() {
        return QEvent.event.state.eq(EventState.PUBLISHED);
    }

    private BooleanExpression byOnlyAvailable(Boolean onlyAvailable) {
        return (onlyAvailable != null && onlyAvailable)
                ? QEvent.event.confirmedRequests.lt(QEvent.event.participantLimit)
                : QEvent.event.id.isNotNull();
    }

}
