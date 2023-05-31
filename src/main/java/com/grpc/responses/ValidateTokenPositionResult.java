package com.grpc.responses;

import com.gpch.grpc.protobuf.Token;
import com.gpch.grpc.protobuf.ValidateTokenPositionResponse;
import responses.Error;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.grpc.domain.Solver.NAMESPACE;

public class ValidateTokenPositionResult {
    private List<com.gpch.grpc.protobuf.Error> errors;
    private ArrayList<String> wordsToSelect;
    private List<Token> studentAnswer;
    private String taskInTTLFormat;

    public ValidateTokenPositionResult(
            List<Error> errors,
            LinkedHashMap<String,
                    String> studentAnswer,
            String taskInTTLFormat,
            ArrayList<String> wordsToSelect
    ) {
        this.taskInTTLFormat = taskInTTLFormat;
        this.wordsToSelect = wordsToSelect;

        List<Token> convertedStudentAnswer = new ArrayList<>();
        Token.Builder builder = Token.newBuilder();
        for (Map.Entry<String, String> word : studentAnswer.entrySet()) {
            builder.setName(word.getKey());
            builder.setId(word.getValue());
            convertedStudentAnswer.add(builder.build());
        };
        this.studentAnswer = convertedStudentAnswer;

        List<com.gpch.grpc.protobuf.Error> convertedErrors = new ArrayList<>();
        com.gpch.grpc.protobuf.Error.Builder errBuilder = com.gpch.grpc.protobuf.Error.newBuilder();
        for (Error err : errors) {
            errBuilder.setText(err.getText());
            errBuilder.setType(err.getType());
            convertedErrors.add(errBuilder.build());
        }
        this.errors = convertedErrors;
    }

    public List<Token> getStudentAnswer() {
        return studentAnswer;
    }

    public String getTaskInTTLFormat() {
        return taskInTTLFormat;
    }

    public List<com.gpch.grpc.protobuf.Error> getErrors() {
        return errors;
    }

    public List<String> getWordsToSelect() {
        return wordsToSelect;
    }
}
