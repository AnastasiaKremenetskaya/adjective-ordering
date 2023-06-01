package com.grpc.responses;

import com.gpch.grpc.protobuf.ErrorPart;
import com.gpch.grpc.protobuf.Token;
import responses.Error;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            builder.setName(word.getValue());
            builder.setId(word.getKey());
            convertedStudentAnswer.add(builder.build());
        };
        this.studentAnswer = convertedStudentAnswer;

        List<com.gpch.grpc.protobuf.Error> convertedErrors = new ArrayList<>();
        List<com.gpch.grpc.protobuf.ErrorPart> convertedErrorParts = new ArrayList<>();
        com.gpch.grpc.protobuf.Error.Builder errBuilder = com.gpch.grpc.protobuf.Error.newBuilder();
        com.gpch.grpc.protobuf.ErrorPart.Builder errPartBuilder = com.gpch.grpc.protobuf.ErrorPart.newBuilder();
        for (Error err : errors) {
            for (responses.ErrorPart errorPart : err.getError()) {
                errPartBuilder.setText(errorPart.getText());
                errPartBuilder.setType(errorPart.getType());
                convertedErrorParts.add(errPartBuilder.build());
            }
            errBuilder.addAllError(convertedErrorParts);
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
