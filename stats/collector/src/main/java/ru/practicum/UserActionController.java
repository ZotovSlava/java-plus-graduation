package ru.practicum;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.dto.UserActionDto;
import ru.practicum.mapper.ProtoMapper;
import ru.practicum.service.CollectorService;
import ru.yandex.practicum.grpc.collector.UserActionControllerGrpc;
import ru.yandex.practicum.grpc.user_action.UserActionProto;

@AllArgsConstructor
@GrpcService
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase{

    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try{
            UserActionDto userActionDto = ProtoMapper.toDto(request);
            collectorService.createUserAction(userActionDto);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
