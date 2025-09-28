package ru.practicum.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.dto.UserActionDto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public class AvroMapper {
    public static SpecificRecordBase toAvro(UserActionDto userActionDto){
        return UserActionAvro.newBuilder()
                .setUserId(userActionDto.getUserId())
                .setEventId(userActionDto.getEventId())
                .setType(ActionTypeAvro.valueOf(userActionDto.getActionType().name()))
                .setTimestamp(userActionDto.getTimestamp())
                .build();
    }
}
