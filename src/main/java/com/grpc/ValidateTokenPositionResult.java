package com.grpc;

import java.util.List;

public class ValidateTokenPositionResult {
    private List<String> errors;
    private String tokenId; // ex. "item_0"

    public ValidateTokenPositionResult(List<String> errors, String tokenId) {
        this.errors = errors;
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public List<String> getErrors() {
        return errors;
    }
}
