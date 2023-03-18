package com.grpc;

import com.google.protobuf.ByteString;
import com.gpch.grpc.protobuf.CheckRDFResponse;
import com.gpch.grpc.protobuf.CheckRDFRequest;
import com.gpch.grpc.protobuf.SentenceCheckerServiceGrpc;
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
            OrderCheckerResult orderCheckerResult = AdjectiveOrderChecker.getAdjectiveOrderingErrors(request.getXmlWithRdf());

            responseObserver.onNext(getRDFFromOrderCheckerResult(orderCheckerResult));
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
}