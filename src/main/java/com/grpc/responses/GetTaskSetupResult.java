package com.grpc.responses;

import java.util.Map;

public class GetTaskSetupResult {
    private String task;
    private Map<String, String> tokens; // ex. "item_0":"The"

    public GetTaskSetupResult(String task, Map<String, String> tokens) {
        this.task = task;
        this.tokens = tokens;
    }

    public String getTask() {
        return task;
    }

    public Map<String, String> getTokens() {
        return tokens;
    }
}
