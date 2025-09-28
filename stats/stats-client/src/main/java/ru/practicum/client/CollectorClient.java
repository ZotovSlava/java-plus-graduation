package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.collector.UserActionControllerGrpc;
import ru.yandex.practicum.grpc.user_action.UserActionProto;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub;

    public void sendUserActionToCollector(UserActionProto record) {
        try {
            collectorStub.collectUserAction(record);
            log.info("Действие пользователя отправлено: userId={}, eventId={}",
                    record.getUserId(), record.getEventId());
        } catch (Exception ignored) {
        }
    }
}