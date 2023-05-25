package com.grpc.responses;

import java.util.List;
import java.util.Map;

public class ValidateTokenPositionResult {
    private List<String> errors;
    private Map<String, String> tokens;
    private String taskInTTLFormat;

    public ValidateTokenPositionResult(List<String> errors, Map<String, String> tokens, String taskInTTLFormat) {
        this.errors = errors;
        this.tokens = tokens;
        this.taskInTTLFormat = taskInTTLFormat;
    }

    public Map<String, String> getTokens() {
        return tokens;
    }

    public String getTaskInTTLFormat() {
        return taskInTTLFormat;
    }

    public List<String> getErrors() {
        return errors;
    }
}
