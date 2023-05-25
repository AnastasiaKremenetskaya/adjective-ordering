package com.grpc;

import com.gpch.grpc.protobuf.*;
import com.grpc.domain.GetTaskSetup;
import com.grpc.domain.ValidateTokenPosition;
import com.grpc.responses.GetTaskSetupResult;
import com.grpc.responses.ValidateTokenPositionResult;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
@Slf4j
public class ServiceImpl extends SentenceCheckerServiceGrpc.SentenceCheckerServiceImplBase {
    @Override
    public void validateTokenPosition(ValidateTokenPositionRequest request, StreamObserver<ValidateTokenPositionResponse> responseObserver) {
        try {
            ValidateTokenPositionResult res = new ValidateTokenPosition(
                    request.getLang(),
                    request.getTaskInTTLFormat(),
                    request.getTokensMap(),
                    request.getTokenToCheck()
            ).checkTokenPosition();


            responseObserver.onNext(getValidateTokenPositionResponse(res));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(Status.ABORTED
                    .withDescription("Unable to load file")
                    .withCause(e)
                    .asException());
        }
    }

    @Override
    public void getTaskSetup(GetTaskSetupRequest request, StreamObserver<GetTaskSetupResponse> responseObserver) {
        try {
            GetTaskSetupResult res = new GetTaskSetup().getTask(request.getTaskInTTLFormat());

            responseObserver.onNext(getTaskResponse(res));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(Status.ABORTED
                    .withDescription("Unable to load file")
                    .withCause(e)
                    .asException());
        }
    }

    private ValidateTokenPositionResponse getValidateTokenPositionResponse(ValidateTokenPositionResult res) {
        ValidateTokenPositionResponse.Builder builder = ValidateTokenPositionResponse.newBuilder();

        builder.addValidationErrors(res.getErrors().toString());
        builder.setTaskInTTLFormat(res.getTaskInTTLFormat());
        builder.putAllTokens(res.getTokens());

        return builder.build();
    }

    private GetTaskSetupResponse getTaskResponse(GetTaskSetupResult res) {
        GetTaskSetupResponse.Builder builder = GetTaskSetupResponse.newBuilder();

        builder.putAllTokens(res.getTokens());

        return builder.build();
    }
}