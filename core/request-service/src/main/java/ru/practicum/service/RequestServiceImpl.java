package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.CollectorClient;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.event.SimpleEventDto;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.RequestNotFoundException;
import ru.practicum.feign.event.EventClient;
import ru.practicum.feign.user.UserClient;
import ru.practicum.mapper.EventUpdateMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.storage.RequestRepository;
import ru.yandex.practicum.grpc.user_action.ActionTypeProto;
import ru.yandex.practicum.grpc.user_action.UserActionProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventAdminClient;
    private final CollectorClient collectorClient;

    @Override
    public RequestDto create(Long userId, Long eventId) {
        UserRequestDto user = userClient.getById(userId);

        SimpleEventDto event = eventAdminClient.getById(eventId);

        if (user.getId().equals(event.getInitiatorId())) {
            throw new ConflictException("You cannot register for your own event.");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("You cannot register in an unpublished event.");
        }

        if (event.getConfirmedRequests().equals(event.getParticipantLimit()) && event.getParticipantLimit() != 0) {
            throw new ConflictException("All spots are taken, registration is not possible.");
        }

        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Вы уже оставили заявку на участие.");
        }

        Request request = new Request();
        request.setRequesterId(user.getId());
        request.setEventId(event.getId());
        request.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventAdminClient.updateConfirmedRequest(EventUpdateMapper.toEventUpdateAdminDto(event), eventId);

            Instant myInstant = Instant.now();
            collectorClient.sendUserActionToCollector(UserActionProto.newBuilder()
                    .setUserId(userId.intValue())
                    .setEventId(eventId.intValue())
                    .setType(ActionTypeProto.REGISTER)
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(myInstant.getEpochSecond())
                            .setNanos(myInstant.getNano())
                            .build())
                    .build()
            );
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            Instant myInstant = Instant.now();
            collectorClient.sendUserActionToCollector(UserActionProto.newBuilder()
                    .setUserId(userId.intValue())
                    .setEventId(eventId.intValue())
                    .setType(ActionTypeProto.REGISTER)
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(myInstant.getEpochSecond())
                            .setNanos(myInstant.getNano())
                            .build())
                    .build()
            );
        }


        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> get(Long userId) {
        UserRequestDto user = userClient.getById(userId);

        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDto update(Long userId, Long requestId) {
        UserRequestDto user = userClient.getById(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toDto(
                requestRepository.save(request)
        );
    }

    @Override
    public RequestDto updateState(Long userId, Long requestId, RequestStatus status) {
        UserRequestDto user = userClient.getById(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        request.setStatus(status);

        return RequestMapper.toDto(
                requestRepository.save(request)
        );
    }

    @Override
    public List<RequestDto> getAllByEventId(Long eventId) {
        SimpleEventDto event = eventAdminClient.getById(eventId);

        return requestRepository.findAllByEventId(eventId).stream().map(RequestMapper::toDto).toList();
    }

    @Override
    public List<RequestDto> getAllByEventIdAndRequestIds(Long eventId, Set<Long> requestIds) {
        return requestRepository.findAllByEventIdAndIdIn(eventId, requestIds).stream()
                .map(RequestMapper::toDto)
                .toList();
    }
}
