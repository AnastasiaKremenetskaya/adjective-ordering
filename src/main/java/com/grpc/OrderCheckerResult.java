package com.grpc;

import java.util.List;

public class OrderCheckerResult {
    private List<String> adjectiveOrderingErrors;
    private List<String> modelValidationErrors;

    public OrderCheckerResult(List<String> adjectiveOrderingErrors, List<String> modelValidationErrors) {
        this.adjectiveOrderingErrors = adjectiveOrderingErrors;
        this.modelValidationErrors = modelValidationErrors;
    }

    public List<String> getAdjectiveOrderingErrors() {
        return adjectiveOrderingErrors;
    }

    public List<String> getModelValidationErrors() {
        return modelValidationErrors;
    }

    public void setAdjectiveOrderingErrors(List<String> adjectiveOrderingErrors) {
        this.adjectiveOrderingErrors = adjectiveOrderingErrors;
    }

    public void setModelValidationErrors(List<String> modelValidationErrors) {
        this.modelValidationErrors = modelValidationErrors;
    }
}
