package com.grpc.responses;

import com.gpch.grpc.protobuf.Token;
import com.gpch.grpc.protobuf.ValidateTokenPositionResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.grpc.domain.Solver.NAMESPACE;

public class ValidateTokenPositionResult {
    private List<String> errors;
    private ArrayList<String> wordsToSelect;
    private List<Token> studentAnswer;
    private String taskInTTLFormat;

    public ValidateTokenPositionResult(
            List<String> errors,
            LinkedHashMap<String,
                    String> studentAnswer,
            String taskInTTLFormat,
            ArrayList<String> wordsToSelect
    ) {
        this.errors = errors;
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

    }

    public List<Token> getStudentAnswer() {
        return studentAnswer;
    }

    public String getTaskInTTLFormat() {
        return taskInTTLFormat;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWordsToSelect() {
        return wordsToSelect;
    }
}
