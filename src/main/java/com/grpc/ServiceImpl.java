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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;

@GRpcService
@Slf4j
public class ServiceImpl extends SentenceCheckerServiceGrpc.SentenceCheckerServiceImplBase {
    @Override
    public void validateTokenPosition(ValidateTokenPositionRequest request, StreamObserver<ValidateTokenPositionResponse> responseObserver) {
        try {
            AtomicReference<String> tokenToCheck = new AtomicReference<>("");
            LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
            request.getStudentAnswerList().forEach((token -> {
                        studentAnswerMap.put(token.getName(), token.getId());
                        if (token.getId().isEmpty()) {
                            tokenToCheck.set(token.getName());
                        }
                    })
            );
            ArrayList<String> wordsToSelectList = new ArrayList<String>(request.getWordsToSelectList());

            ValidateTokenPositionResult res = ValidateTokenPosition.getInstance().
                    checkTokenPosition(
                            request.getLang(),
                            request.getTaskInTTLFormat(),
                            studentAnswerMap,
                            tokenToCheck.get(),
                            wordsToSelectList
                    );

            responseObserver.onNext(getValidateTokenPositionResponse(res));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(Status.ABORTED
                    .withDescription(e.getMessage())
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

        builder.addAllErrors(res.getErrors());
        builder.setTaskInTTLFormat(res.getTaskInTTLFormat());
        builder.addAllStudentAnswer(res.getStudentAnswer());
        builder.addAllWordsToSelect(res.getWordsToSelect());

        return builder.build();
    }

    private GetTaskSetupResponse getTaskResponse(GetTaskSetupResult res) {
        GetTaskSetupResponse.Builder builder = GetTaskSetupResponse.newBuilder();

        builder.putAllTokens(res.getTokens());

        return builder.build();
    }
}