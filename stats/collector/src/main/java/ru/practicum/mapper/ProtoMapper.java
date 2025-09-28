package ru.practicum.mapper;

import ru.practicum.dto.ActionType;
import ru.practicum.dto.UserActionDto;
import ru.yandex.practicum.grpc.user_action.UserActionProto;

import java.time.Instant;

public class ProtoMapper {
    public static UserActionDto toDto(UserActionProto userActionProto) {
        return UserActionDto.builder().
                userId(userActionProto.getUserId()).
                eventId(userActionProto.getEventId()).
                actionType(ActionType.valueOf(userActionProto.getType().name())).
                timestamp(Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()))
                .build();
    }
}
