package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestEventDto;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EventResultRequestStatusDto {

    private List<RequestDto> confirmedRequests;

    private List<RequestDto> rejectedRequests;
}
