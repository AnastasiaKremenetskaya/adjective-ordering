package com.grpc;

import com.google.protobuf.ByteString;
import com.gpch.grpc.protobuf.CheckRDFResponse;
import com.gpch.grpc.protobuf.CheckRDFRequest;
import com.gpch.grpc.protobuf.ValidateTokenPositionRequest;
import com.gpch.grpc.protobuf.ValidateTokenPositionResponse;
import com.gpch.grpc.protobuf.SentenceCheckerServiceGrpc;
import com.grpc.ServiceImpl;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@GRpcService
@Slf4j
public class ServiceImpl extends SentenceCheckerServiceGrpc.SentenceCheckerServiceImplBase {

    @Override
    public void checkRDF(CheckRDFRequest request, StreamObserver<CheckRDFResponse> responseObserver) {
        try {
//            OrderCheckerResult orderCheckerResult = AdjectiveOrderChecker.getAdjectiveOrderingErrors(request.getXmlWithRdf());
//            OrderCheckerResult orderCheckerResult = new Solver().solve();
//            OrderCheckerResult orderCheckerResult  = new StudentResponseFormatter(1, ).solve();


//            responseObserver.onNext(getRDFFromOrderCheckerResult(orderCheckerResult));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(Status.ABORTED
                    .withDescription("Unable to load file")
                    .withCause(e)
                    .asException());
        }
    }

    @Override
    public void validateTokenPosition(ValidateTokenPositionRequest request, StreamObserver<ValidateTokenPositionResponse> responseObserver) {
        try {
            OrderCheckerResult orderCheckerResult = new StudentResponseFormatter(
                    request.getNumberOfTask(),
                    request.getTokensMap(),
                    request.getTokenToCheck()
            ).checkTokenPosition();


            responseObserver.onNext(getValidateResponseFromOrderCheckerResult(orderCheckerResult));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(Status.ABORTED
                    .withDescription("Unable to load file")
                    .withCause(e)
                    .asException());
        }
    }

    private CheckRDFResponse getRDFFromOrderCheckerResult(OrderCheckerResult orderCheckerResult) {
        CheckRDFResponse.Builder builder = CheckRDFResponse.newBuilder();

        builder.addAllAdjOrderingErrors(orderCheckerResult.getAdjectiveOrderingErrors());
        builder.addAllRdfModelValidationErrors(orderCheckerResult.getModelValidationErrors());

        return builder.build();
    }

    private ValidateTokenPositionResponse getValidateResponseFromOrderCheckerResult(OrderCheckerResult orderCheckerResult) {
        ValidateTokenPositionResponse.Builder builder = ValidateTokenPositionResponse.newBuilder();

        builder.setError(orderCheckerResult.getAdjectiveOrderingErrors().toString());

        return builder.build();
    }
}